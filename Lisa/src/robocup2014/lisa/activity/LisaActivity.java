package robocup2014.lisa.activity;

import robocup2014.lisa.connection.ConnectedThread;
import robocup2014.lisa.connection.Globals;
import robocup2014.lisa.datatype.Robot;
import robocup2014.lisa.datatype.Tile;

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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

/**
 * Created by luca on 2/12/14.
 */
public class LisaActivity extends Activity {

	private ConnectedThread ct;	
	private String lastData = "";
	boolean dataReady = false;
	private Robot lisa = new Robot();
	private int x_field_dim = 20,
			y_field_dim = 20;
	private int cell_dim = 0,
			x_margin = 0,
			y_margin = 0,
			strokeWidth = 3;
	private int robot_z = 0;
	private DrawView mazeView;
	private Display mDisplay;
	private Point displaySize = new Point();
	private Boolean horizontal = false,
			followRobot = false;
	private Switch switchFollowRobot;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mazeView = new DrawView(this);
		setContentView(mazeView);
		mazeView.setBackgroundColor(Color.GRAY);

		mazeView.setOnTouchListener(mazeViewTouchListener);
		// Show the Up button in the action bar.
		setupActionBar();

		ct = Globals.getConnectedThread();
		ct.setHandler(myHandler);
		if (ct.getState() == Thread.State.NEW) {
			ct.start();
		}

		mDisplay = getWindowManager().getDefaultDisplay();
		mDisplay.getSize(displaySize);

		// Toast.makeText(getApplicationContext(), displaySize.x+":"+
		// displaySize.y,Toast.LENGTH_LONG).show();
		if (displaySize.x > displaySize.y) {
			horizontal = true;
		}

		mazeView.setDisplaySize();
		mazeView.setShowFirstFloor(true);
		mazeView.update();
		formatText("aaa1101000");
		setTile(lastData);
		Log.d("dir to go", lisa.getNextDir().toString());
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lisa, menu);

		switchFollowRobot = (Switch) menu.findItem(R.id.follow_robot).getActionView();
		switchFollowRobot.setChecked(false);
		switchFollowRobot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					followRobot = true;
					Toast.makeText(getApplicationContext(), "Following Lisa", Toast.LENGTH_SHORT).show();
				}
				else {
					followRobot = false;
					Toast.makeText(getApplicationContext(), "Unfollowing Lisa", Toast.LENGTH_SHORT).show();
				}
				mazeView.update();
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
		case R.id.change_floor:
			mazeView.setShowFirstFloor(!mazeView.getShowFirstFloor());
			mazeView.update();
			return true;
		case R.id.refresh:
			mazeView.update();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//cosi facendo quando viene premuto il tasto back l'activity passa in background
	public void onBackPressed () {
		moveTaskToBack (true);
	}
	// Handler
	private final Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case Globals.RA_MSG_READ:
				formatText((String) msg.obj);
				if(dataReady){
					setTile(lastData);
					Log.d("dir to go", lisa.getNextDir().toString());
				}
				Log.d("handler", dataReady + " : " + lastData);
				break;
			}
		}
	};

	private void formatText(String input) {

		String data = "";
		for (int i = 0; i < input.length(); i++) {
			Character c = input.charAt(i);
			if(c == '#'){
				lastData = "";
				dataReady = false;
			}
			else if(c == '*') {
				dataReady = true;
			}
			else if(c != 0) {
				data += c;
			}
		}
		lastData += data;
	}

	private void setTile(String data){

		int x = data.charAt(0) - 'a',
				y = data.charAt(1) - 'a';
		boolean level = data.charAt(2) == 'a'? true : false;
		Tile.Floor floor;
		switch(data.charAt(7)){
		case '1': floor = Tile.Floor.SILVER_FLOOR;
		break;
		case '2': floor = Tile.Floor.BLACK_FLOOR;
		break;
		default: floor = Tile.Floor.WHITE_FLOOR;
		break;
		}
		lisa.setLevel(level);
		mazeView.setShowFirstFloor(level);
		lisa.setPosition(new Point(x + lisa.startingPos, y + lisa.startingPos));
		lisa.setFloor(floor);
		lisa.setVictim(data.charAt(8) == '1'? true : false);
		lisa.setObstacle();
		lisa.setWalls(data.charAt(3) == '1'? true : false,
				data.charAt(4) == '1'? true : false,
						data.charAt(5) == '1'? true : false,
								data.charAt(6) == '1'? true : false);

		mazeView.update();
	}

	public Tile[][] getMazeGround(){
		return lisa.getMazeGround();
	}
	public Tile[][] getMazeAir(){
		return lisa.getMazeAir();
	}
	public boolean getLevel(){
		return lisa.getLevel();
	}
	public Point getPosition(){
		return lisa.getPosition();
	}

	private DrawView.OnTouchListener mazeViewTouchListener = new View.OnTouchListener() {
		Point[] startPoint = new Point[2];
		Point[] endPoint = new Point[2];
		Point[] midPoint = new Point[2];
		int mode = 0,
				DRAG = 1,
				ZOOM = 2;

		final int DOWN_0 = 5,
				DOWN_1 = 261,
				DOWN_2 = 517,
				UP_0 = 6,
				UP_1 = 262,
				UP_2 = 518;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			boolean toRtn = false;

			if(!mazeView.getShowFirstFloor() || followRobot) { return false; }

			Log.d("Event", event.getAction() + event.toString());
			switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mode = DRAG;
				startPoint[0] = new Point((int) event.getX(0), (int) event.getY(0));
				toRtn = true;
				break;
			case DOWN_0:case DOWN_1:case DOWN_2:
				startPoint[1] = new Point((int) event.getX(1), (int) event.getY(1));
				midPoint[0] = midPoint(startPoint[0], startPoint[1]);
				mode = ZOOM;
				toRtn = true;
				break;
			case MotionEvent.ACTION_MOVE:
				Point displacement;
				endPoint[0] = new Point((int) event.getX(0), (int) event.getY(0));

				if(mode == DRAG) {
					displacement = new Point(endPoint[0].x - startPoint[0].x, endPoint[0].y - startPoint[0].y);
					mazeView.addDisplacement(displacement);

					startPoint[0] = endPoint[0];
				}
				else if(mode == ZOOM) {
					endPoint[1] = new Point((int) event.getX(1), (int) event.getY(1));
					midPoint[1] = midPoint(endPoint[0], endPoint[1]);

					displacement = new Point(midPoint[1].x - midPoint[0].x, midPoint[1].y - midPoint[0].y);
					mazeView.addDisplacement(displacement);

					float d1 = distance(endPoint[0], endPoint[1]);
					float d2 = distance(startPoint[0], startPoint[1]);
					float scale = d1 / d2;
					mazeView.addScale(scale);
					Log.d("Range", scale +"::"+ endPoint[0] +"::"+ endPoint[1] +"::"+ startPoint[0] +"::"+ startPoint[1]);

					midPoint[0] = midPoint[1];
					startPoint[0] = endPoint[0];
					startPoint[1] = endPoint[1];
				}
				toRtn = true;
				break;
			case UP_0:case UP_1:case UP_2:
				//endPoint[1] = new Point((int) event.getX(1), (int) event.getY(1));
				mode = DRAG;
				toRtn = true;
				break;
			case MotionEvent.ACTION_UP:
				toRtn = true;
				break;
			}

			mazeView.update();
			return toRtn;
		}
	};

	private int midPoint(float a, float b) {
		return (int) ((a + b) / 2);
	}

	private Point midPoint(Point a, Point b) {
		Point toRtn = new Point();

		toRtn.x = midPoint(a.x, b.x);
		toRtn.y = midPoint(a.y, b.y);

		return toRtn;
	}

	private float distance(Point a, Point b) {
		float distance = 0;

		float x = a.x - b.x;
		float y = a.y - b.y;

		distance = (float) Math.sqrt((x * x) + (y * y));
		//Log.d("Range", distance +"::"+ x +"::"+ y);

		return distance;
	}

	private class DrawView extends View {

		private float scale = 1;
		private Point mazeCenter;
		private boolean showFirstFloor = false;
		private Tile[][] maze;
		private int z = 0;
		private Paint paint = new Paint(),
				rect_paint = new Paint(),
				obstacle_paint = new Paint(),
				wall_paint = new Paint();
		private Point displaySize = new Point();

		public DrawView(Context context) {
			super(context);
			paint.setColor(Color.BLACK);
		}

		@Override
		public void onDraw(Canvas canvas) {
			DrawField(canvas);
		}

		public void setShowFirstFloor(boolean b) {
			showFirstFloor = b;

			if(getWidth() > 0 ) {
				setDisplaySize(new Point(getWidth(), getHeight()));
			}

			if(showFirstFloor) {
				x_field_dim = lisa.MAX_GROUND;
				y_field_dim = lisa.MAX_GROUND;
				Toast.makeText(getApplicationContext(), "Ground Level", Toast.LENGTH_SHORT).show();
			}
			else {
				x_field_dim = lisa.MAX_AIR;
				y_field_dim = lisa.MAX_AIR;
				Toast.makeText(getApplicationContext(), "Air Level", Toast.LENGTH_SHORT).show();
			}

			if (horizontal) {
				cell_dim = (displaySize.y - 4) / (y_field_dim);
				x_margin = (displaySize.x - (cell_dim * x_field_dim)) / 2;
				y_margin = (displaySize.y - (cell_dim * y_field_dim)) / 2;
			} else {
				cell_dim = (displaySize.x - 4) / (x_field_dim);
				x_margin = (displaySize.x - (cell_dim * x_field_dim)) / 2;
				y_margin = (displaySize.y - (cell_dim * y_field_dim)) / 2;
			}

			Log.d("Drawing", x_field_dim+":"+y_field_dim+":"+x_margin+":"+y_margin+":"
					+cell_dim+":"+displaySize.x+":"+displaySize.y);


			strokeWidth = cell_dim / 8;

			strokeWidth = limitValue(3, strokeWidth, 10);

			wall_paint.setStrokeWidth(strokeWidth);
			obstacle_paint.setColor(Color.BLACK);
			obstacle_paint.setStrokeWidth(3);

			if (showFirstFloor) {
				maze = new Tile[lisa.MAX_GROUND][lisa.MAX_GROUND];
				maze = lisa.getMazeGround();
				wall_paint.setColor(Color.RED);
			} else {
				maze = new Tile[lisa.MAX_AIR][lisa.MAX_AIR];
				maze = lisa.getMazeAir();
				wall_paint.setColor(Color.MAGENTA);
			}
		}

		public boolean getShowFirstFloor() {
			return showFirstFloor;
		}

		public void setDisplaySize() {
			mDisplay.getSize(displaySize);
			mazeCenter = new Point(displaySize.x/2, (displaySize.y - 150)/2);
		}

		public void setDisplaySize(Point p) {
			displaySize = p;
		}

		public void setScale(double s) {
			scale = (float) s;

			scale = limitValue(1, scale, 3);
		}

		public void addScale(double s) {
			scale = (float) (scale * s);
			//            if(showFirstFloor) {
				//                scale = scaleFirstFloor;
				//            }
			scale = limitValue(1, scale, 3);
		}

		public void setCenter(Point newCenter) {
			mazeCenter.x = newCenter.x;
			mazeCenter.y = newCenter.y;
		}

		public void addDisplacement(Point d) {
			Log.d("Displacement", d.toString());
			mazeCenter.x -= (d.x * 2) / scale;
			mazeCenter.y -= (d.y * 2) / scale;
			mazeCenter.x = limitValue(x_margin, mazeCenter.x, getWidth() - x_margin);
			mazeCenter.y = limitValue(y_margin, mazeCenter.y, getHeight() - y_margin);
		}

		public int limitValue(int minBound, int value, int maxBound) {
			if(value < minBound) {
				return minBound;
			}
			else if(value > maxBound) {
				return maxBound;
			}
			else {
				return value;
			}
		}

		public float limitValue(int minBound, float value, int maxBound) {
			if(value < minBound) {
				return minBound;
			}
			else if(value > maxBound) {
				return maxBound;
			}
			else {
				return value;
			}
		}

		public void DrawField(Canvas canvas) {


			int rob_radius = cell_dim / 2,
					rob_x = (lisa.getPosition().x * cell_dim) + x_margin + rob_radius,
					rob_y = (lisa.getPosition().y * cell_dim) + y_margin + rob_radius;

			if(followRobot) {
				mazeCenter.x = rob_x;
				mazeCenter.y = rob_y;
			}

			if(showFirstFloor) {
				canvas.scale(scale, scale, mazeCenter.x, mazeCenter.y);
			}
			else {
				canvas.scale(1, 1);
			}

			Log.d("Scaling", scale + ":" + mazeCenter.x + ":" + mazeCenter.y);

			for (int x = 0; x < x_field_dim; x++) {
				for (int y = 0; y < y_field_dim; y++) {
					Tile tile = maze[x][y];

					rect_paint.setColor(Color.WHITE);

					if (tile.getFloor() == Tile.Floor.SILVER_FLOOR) {
						rect_paint.setColor(Color.GRAY);
					}
					else if (tile.getFloor() == Tile.Floor.BLACK_FLOOR) {
						rect_paint.setColor(Color.BLACK);
					}
					else if (tile.getVictim()) {
						rect_paint.setColor(Color.GREEN);
					}

					int start_rx = (x * cell_dim) + x_margin, start_ry = (y * cell_dim)
							+ y_margin, stop_rx = start_rx + cell_dim, stop_ry = start_ry
							+ cell_dim;

					// Toast.makeText(getApplicationContext(),start_rx+":"+start_ry,Toast.LENGTH_LONG).show();
					canvas.drawRect(start_rx, invert(stop_ry), stop_rx,
							invert(start_ry), rect_paint);

					if (tile.getObstacle()) {
						int start_x = (x * cell_dim) + x_margin, start_y = (y * cell_dim)
								+ y_margin, stop_x = start_x + cell_dim, stop_y = start_y
								+ cell_dim;

						canvas.drawLine(start_x, invert(start_y), stop_x,
								invert(stop_y), obstacle_paint);

						start_y = (y * cell_dim) + y_margin + cell_dim;
						stop_y = start_y - cell_dim;

						canvas.drawLine(start_x, invert(start_y), stop_x,
								invert(stop_y), obstacle_paint);
					}
				}
			}

			for (int x = 0; x < x_field_dim; x++) {
				for (int y = 0; y < y_field_dim; y++) {
					Tile tile = maze[x][y];

					if (tile.getWall(Tile.Direction.BACK)) {
						int start_x = (x * cell_dim) + x_margin, start_y = (y * cell_dim)
								+ y_margin, stop_x = start_x + cell_dim, stop_y = start_y;

						canvas.drawLine(start_x, invert(start_y), stop_x,
								invert(stop_y), wall_paint);
					}

					if (tile.getWall(Tile.Direction.LEFT)) {
						int start_x = (x * cell_dim) + x_margin,
								start_y = (y * cell_dim) + y_margin,
								stop_x = start_x,
								stop_y = start_y + cell_dim;

						canvas.drawLine(start_x, invert(start_y), stop_x,
								invert(stop_y), wall_paint);
					}

					if (tile.getWall(Tile.Direction.RIGHT)) {
						int start_x = (x * cell_dim) + x_margin + cell_dim, start_y = (y * cell_dim)
								+ y_margin, stop_x = start_x, stop_y = start_y
								+ cell_dim;

						canvas.drawLine(start_x, invert(start_y), stop_x,
								invert(stop_y), wall_paint);
					}

					if (tile.getWall(Tile.Direction.AHEAD)) {
						int start_x = (x * cell_dim) + x_margin, start_y = (y * cell_dim)
								+ y_margin + cell_dim, stop_x = start_x
								+ cell_dim, stop_y = start_y;

						canvas.drawLine(start_x, invert(start_y), stop_x,
								invert(stop_y), wall_paint);
					}

					int px = (x * cell_dim) + x_margin, py = (y * cell_dim)
							+ y_margin;

					canvas.drawCircle(px, invert(py), strokeWidth / 2,
							wall_paint);

					px += cell_dim;

					canvas.drawCircle(px, invert(py), strokeWidth / 2,
							wall_paint);

					px -= cell_dim;
					py += cell_dim;

					canvas.drawCircle(px, invert(py), strokeWidth / 2,
							wall_paint);

					px += cell_dim;

					canvas.drawCircle(px, invert(py), strokeWidth / 2,
							wall_paint);
				}
			}

			if (robot_z == z) {
				canvas.drawCircle(rob_x, invert(rob_y), rob_radius / 2,
						wall_paint);
			}
		}

		private int invert(int distance) {
			return getHeight() - distance;
		}

		public void update() {
			invalidate();
		}
	}
}	