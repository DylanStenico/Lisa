package robocup2014.lisa.activity;

import robocup2014.lisa.connection.ConnectedThread;
import robocup2014.lisa.connection.Globals;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MazeActivity extends Activity {

    //Thread that manages the Bluetooth connection
    private ConnectedThread ct;

    //Strings used in the protocol
    private String CM_BEGIN = "#",
            CM_STOP = "*",
            CM_NULL = "&";

    private char CM_FALSE = '0',
            CM_TRUE = '1',
            CM_EMPTY = '2',
            CM_SILVER = '1',
            CM_BLACK = '2';

    private int x_field_dim = 20,
            y_field_dim = 20,
            z_field_dim = 2;

    private int robot_x = 10,
            robot_y = 10,
            robot_z = 0;

    private String received_value = CM_NULL;

    private Cell[][][] Field = new Cell[x_field_dim][y_field_dim][z_field_dim];

    private DrawView dView;
    private Canvas mCanvas = new Canvas();
    private Display mDisplay;
    private Point displaySize = new Point();
    private Boolean horizontal = false;

    private Button bShowFirstFloor;
    private Boolean showFirstFloor = true;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dView  = new DrawView(this);
        setContentView(dView);
        dView.setBackgroundColor(Color.GRAY);

        // Show the Up button in the action bar.
        setupActionBar();

        ct = Globals.getConnectedThread();
        ct.setHandler(myHandler);
        /*if (!ct.isAlive())
            ct.start();*/
        if(ct.getState() == Thread.State.NEW) {
            ct.start();
        }

        mDisplay = getWindowManager().getDefaultDisplay();
        mDisplay.getSize(displaySize);
        //Toast.makeText(getApplicationContext(), displaySize.x+":"+ displaySize.y,Toast.LENGTH_LONG).show();
        if(displaySize.x > displaySize.y) {
            horizontal = true;
        }

        SetupField();
        check_old_text();

        //Field[0][0][0].setCell("#aaa0111100*");
        //Field[1][0][0].setCell("#aaa0111010*");
        //Field[2][0][0].setCell("#aaa0111001*");
    }

    private void SetupField() {
        for (int x=0;x < x_field_dim;x++) {
            for (int y=0; y < y_field_dim;y++) {
                for(int z=0; z < z_field_dim;z++) {
                    Field[x][y][z] = new Cell(x,y,z);
                }
            }
        }
    }

    //auto build
    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.maze, menu);

        bShowFirstFloor = (Button) menu.findItem(R.id.change_floor).getActionView();
        bShowFirstFloor.setText("Second Floor");
        bShowFirstFloor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showFirstFloor) {
                    showFirstFloor = false;
                    bShowFirstFloor.setText("First Floor");
                }
                else {
                    showFirstFloor = true;
                    bShowFirstFloor.setText("Second Floor");
                }
                dView.invalidate();
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.maze_clear_log:
                Globals.setCompleteString("");
                SetupField();
                dView.invalidate();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //Handler
    private final Handler myHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case Globals.RA_MSG_READ:
                    /*String read_string=(String) msg.obj;

                    if(received_value.equals(CM_NULL)) {
                        if(read_string.contains(CM_BEGIN)) {
                            received_value = read_string.substring(read_string.indexOf(CM_BEGIN));
                        }
                    }
                    else {
                        received_value += read_string;
                    }

                    Toast.makeText(getApplicationContext(),read_string,Toast.LENGTH_LONG).show();
                    if(received_value.length() >= 12) {
                        Log.d("MOOOOOOOOOOOONAAAAAAA",received_value.substring(10,12));
                    }
                    if(received_value.substring(0,1).equals(CM_BEGIN) && received_value.substring(11,12).equals(CM_STOP)) {
                        //Toast.makeText(getApplicationContext(),received_value,Toast.LENGTH_LONG).show();
                        UpdateCell(received_value);

                        received_value = CM_NULL;
                    }*/

                    check_old_text();
                    break;
            }
        }
    };

    private void UpdateCell(char[] data) {
        int x = getInt(data[1]);
        int y = getInt(data[2]);
        int z = getInt(data[3]);

        Field[x][y][z].setCell(data);
    }

    private class Cell {
        private int X,
                Y,
                Z;

        private char North_Wall,
                East_Wall,
                South_Wall,
                West_Wall;

        private char DarkFloor,
                Victim,
                Obstacle;

        Cell(int x, int y, int z) {
            X = x;
            Y = y;
            Z = z;

            North_Wall = CM_FALSE;
            East_Wall = CM_FALSE;
            South_Wall = CM_FALSE;
            West_Wall = CM_FALSE;

            DarkFloor = CM_FALSE;
            Victim = CM_FALSE;
            Obstacle = CM_FALSE;
        }

        Cell(String cell_info) {
            setCell(cell_info);
        }

        public void setCell(String cell_info) {
            //X = GetCoordinate(cell_info.charAt(1));
            //Y = GetCoordinate(cell_info.charAt(2));
            //Z = GetCoordinate(cell_info.charAt(3));

            West_Wall = cell_info.charAt(4);
            North_Wall = cell_info.charAt(5);
            East_Wall = cell_info.charAt(6);
            South_Wall = cell_info.charAt(7);

            DarkFloor = cell_info.charAt(8);
            Victim = cell_info.charAt(9);
            Obstacle = cell_info.charAt(10);

            update();
        }

        public void setCell(char[] data) {
            robot_x = getInt(data[1]);
            robot_y = getInt(data[2]);
            robot_z = getInt(data[3]);

            West_Wall = data[4];
            North_Wall = data[5];
            East_Wall = data[6];
            South_Wall = data[7];

            DarkFloor = data[8];
            Victim = data[9];
            Obstacle = data[10];

            update();
        }

        public char getNorth_Wall() {
            return North_Wall;
        }

        public char getEast_Wall() {
            return East_Wall;
        }

        public char getSouth_Wall() {
            return South_Wall;
        }

        public char getWest_Wall() {
            return West_Wall;
        }

        public char getDarkFloor() {
            return DarkFloor;
        }

        public char getVictim() {
            return Victim;
        }

        public char getObstacle() {
            return Obstacle;
        }

        private void update() {
            //Toast.makeText(getApplicationContext(),North_Wall+":"+East_Wall+":"+South_Wall+":"+West_Wall,Toast.LENGTH_LONG).show();
            dView.invalidate();
        }
    }

    private class DrawView extends View {

        Paint paint = new Paint();

        public DrawView(Context context) {
            super(context);
            paint.setColor(Color.BLACK);
        }

        @Override
        public void onDraw(Canvas canvas) {
            DrawField(canvas);
        }

        public void DrawField(Canvas canvas) {
            Paint rect_paint = new Paint();
            int cell_dim,
                    x_margin,
                    y_margin,
                    strokeWidth;

            if(horizontal) {
                cell_dim = (getHeight() - 20) / (y_field_dim);
                x_margin = (getWidth() - (cell_dim * x_field_dim)) / 2;
                y_margin = 10;
            }
            else {
                cell_dim = (getWidth() - 20) / (x_field_dim);
                x_margin = 10;
                y_margin = (getHeight() - (cell_dim * y_field_dim)) / 2;
            }

            strokeWidth = cell_dim / 8;

            if(strokeWidth < 3) {
                strokeWidth = 3;
            }

            int z = 0;

            Paint wall_paint = new Paint();
            wall_paint.setStrokeWidth(strokeWidth);

            Paint obstacle_paint = new Paint();
            obstacle_paint.setColor(Color.BLACK);
            obstacle_paint.setStrokeWidth(3);

            if(showFirstFloor) {
                z = 0;
                wall_paint.setColor(Color.RED);
            }
            else {
                z = 1;
                wall_paint.setColor(Color.MAGENTA);
            }

            for(int x=0;x<x_field_dim;x++) {
                for(int y=0;y<y_field_dim;y++) {
                    Cell cell = Field[x][y][z];

                    rect_paint.setColor(Color.WHITE);

                    if(cell.getDarkFloor() == CM_SILVER) {
                        rect_paint.setColor(Color.GRAY);
                    }
                    else if(cell.getDarkFloor() == CM_BLACK) {
                        rect_paint.setColor(Color.BLACK);
                    }
                    else if(cell.getVictim() == CM_TRUE) {
                        rect_paint.setColor(Color.GREEN);
                    }

                    int start_rx = (x*cell_dim)+x_margin,
                            start_ry = (y*cell_dim)+y_margin,
                            stop_rx = start_rx+cell_dim,
                            stop_ry = start_ry+cell_dim;

                    //Toast.makeText(getApplicationContext(),start_rx+":"+start_ry,Toast.LENGTH_LONG).show();
                    canvas.drawRect(start_rx, invert(stop_ry), stop_rx, invert(start_ry), rect_paint);

                    if(cell.getObstacle() == CM_TRUE) {
                        int start_x = (x*cell_dim)+x_margin,
                                start_y = (y*cell_dim)+y_margin,
                                stop_x = start_x+cell_dim,
                                stop_y = start_y+cell_dim;

                        canvas.drawLine(start_x,invert(start_y),stop_x,invert(stop_y),obstacle_paint);

                        start_y = (y*cell_dim)+y_margin+cell_dim;
                        stop_y = start_y-cell_dim;

                        canvas.drawLine(start_x,invert(start_y),stop_x,invert(stop_y),obstacle_paint);
                    }
                }
            }

            for(int x=0;x<x_field_dim;x++) {
                for(int y=0;y<y_field_dim;y++) {
                    Cell cell = Field[x][y][z];

                    if(cell.getSouth_Wall() == CM_TRUE) {
                        int start_x = (x*cell_dim)+x_margin,
                                start_y = (y*cell_dim)+y_margin,
                                stop_x = start_x+cell_dim,
                                stop_y = start_y;

                        canvas.drawLine(start_x,invert(start_y),stop_x,invert(stop_y),wall_paint);
                    }

                    if(cell.getWest_Wall() == CM_TRUE) {
                        int start_x = (x*cell_dim)+x_margin,
                                start_y = (y*cell_dim)+y_margin,
                                stop_x = start_x,
                                stop_y = start_y+cell_dim;

                        canvas.drawLine(start_x,invert(start_y),stop_x,invert(stop_y),wall_paint);
                    }

                    if(cell.getEast_Wall() == CM_TRUE) {
                        int start_x = (x*cell_dim)+x_margin+cell_dim,
                                start_y = (y*cell_dim)+y_margin,
                                stop_x = start_x,
                                stop_y = start_y+cell_dim;

                        canvas.drawLine(start_x,invert(start_y),stop_x,invert(stop_y),wall_paint);
                    }

                    if(cell.getNorth_Wall() == CM_TRUE) {
                        int start_x = (x*cell_dim)+x_margin,
                                start_y = (y*cell_dim)+y_margin+cell_dim,
                                stop_x = start_x+cell_dim,
                                stop_y = start_y;

                        canvas.drawLine(start_x,invert(start_y),stop_x,invert(stop_y),wall_paint);
                    }

                    int px = (x*cell_dim)+x_margin,
                            py = (y*cell_dim)+y_margin;

                    canvas.drawCircle(px,invert(py),strokeWidth/2,wall_paint);

                    px += cell_dim;

                    canvas.drawCircle(px,invert(py),strokeWidth/2,wall_paint);

                    px -= cell_dim;
                    py += cell_dim;

                    canvas.drawCircle(px,invert(py),strokeWidth/2,wall_paint);

                    px += cell_dim;

                    canvas.drawCircle(px,invert(py),strokeWidth/2,wall_paint);
                }
            }

            int rob_radius = cell_dim/2,
                    rob_x = (robot_x*cell_dim)+x_margin+rob_radius,
                    rob_y = (robot_y*cell_dim)+y_margin+rob_radius;

            if(robot_z == z) {
                canvas.drawCircle(rob_x,invert(rob_y),rob_radius/2,wall_paint);
            }
        }

        private int invert(int distance) {
            return getHeight() - distance;
        }
    }

    private int getInt(char c) {
        int i = 0;

        switch (c) {
            case 'a':
                i = 0;
                break;
            case 'b':
                i = 1;
                break;
            case 'c':
                i = 2;
                break;
            case 'd':
                i = 3;
                break;
            case 'e':
                i = 4;
                break;
            case 'f':
                i = 5;
                break;
            case 'g':
                i = 6;
                break;
            case 'h':
                i = 7;
                break;
            case 'i':
                i = 8;
                break;
            case 'j':
                i = 9;
                break;
            case 'k':
                i = 10;
                break;
            case 'l':
                i = 11;
                break;
            case 'm':
                i = 12;
                break;
            case 'n':
                i = 13;
                break;
            case 'o':
                i = 14;
                break;
            case 'p':
                i = 15;
                break;
            case 'q':
                i = 16;
                break;
            case 'r':
                i = 17;
                break;
            case 's':
                i = 18;
                break;
            case 't':
                i = 19;
                break;
        }

        return i;
    }

    private void check_old_text() {
        String old_string = Globals.getCompleteString();
        char[] data = new char[12];
        int data_index = 0;
        Boolean data_started = false;
        String data_string;

        if(old_string.length() > 11) {
            for(int i=0;i<old_string.length();i++) {
                char c = old_string.charAt(i);
                int ascii = c;

                if(ascii != 0) {

                    if(ascii == 35) {    //se il carattere equivale a 35 = #
                        data[data_index] = c;
                        data_index++;
                        data_started = true;
                    }
                    else if(data_started) {

                        if(ascii == 48 || ascii == 49 || ascii == 50 || (ascii >= 97 && ascii <= 122)) { //48 = 0 , 49 = 1
                            data[data_index] = c;
                            data_index++;
                        }

                    }
                    else if(ascii == 42) { // 42 = *
                        data[data_index] = c;
                        UpdateCell(data);
                        data_index = 0;
                        data_started = false;
                    }

                    if(data_index >= 11) {
                        UpdateCell(data);
                        data_index = 0;
                        data_started = false;
                    }

                    data_string = ""+data[0]+data[1]+data[2]+data[3]+data[4]+data[5]+data[6]+data[7]+data[8]+data[9]+data[10]+data[11];
                    Log.d("MAZE------------------",data_string);
                }
            }
        }
    }
}