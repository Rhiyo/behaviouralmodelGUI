package com.behaviouralmodel.gui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

public class BMGui extends ApplicationAdapter implements InputProcessor {
	
	public static enum SelectionMode{
		Normal, PlaceBuilding, DoorPlacing, UnitPlacing
	}
	
	//The size of the simulation
	int gridWidth;
	int gridHeight;
	SelectionMode currentMode = SelectionMode.Normal;
	
	//When starting to build building
	Vector2 buildingStart;
	Building newBuilding;
	
	ShapeRenderer shapeRenderer;
	OrthographicCamera camera;
	private Vector3 _lastMouseWorldMovePos;
	private Vector3 _lastMouseWorldDragPos;
	private Vector3 _lastMouseWorldPressPos;
	private Vector3 _lastMouseWorldReleasePos;
	private Vector3 _lastMouseScreenPos;
	   
	static final int CELL_SIZE = 32;
	static final int PADDING_SIZE = 10;
	private int _mouseButtonDown;
	
	//Test unit list
	Array<Array<Vector2>> squads;
	Array<Vector2> units;
	
	//Test buildings
	private class Building{
		public Vector2 p0;
		public Vector2 door;
		public int width;
		public int height;
		public Building(Vector2 p0, int width, int height){
			this.p0 = p0;
			this.width = width;
			this.height = height;
		}
	}
	Array<Building> buildings;
	
	//Create stage for UI
	Stage stage;
	Label mousePosLbl;
	ImageTextButton createBuildingBtn;
	ImageTextButton createUnitBtn;
	Table buildingScrollTable;
	Table unitScrollTable;
    LabelStyle textStyle;
    
	//Current grid pos hovered over 
	Vector2 hoverGridPos;
	
	@Override
	public void create () {
		this.gridWidth = 40;
		this.gridHeight = 30;
		this.shapeRenderer = new ShapeRenderer();
	    float aspectRatio = (float)Gdx.graphics.getHeight()/(float)Gdx.graphics.getWidth();

	    camera = new OrthographicCamera(CELL_SIZE * aspectRatio ,CELL_SIZE);
	      camera.position.set(gridWidth/2,gridHeight/2,0);

	      //controls
	      _lastMouseWorldMovePos = new Vector3();
	      _lastMouseWorldDragPos = new Vector3();
	      _lastMouseWorldPressPos = new Vector3();
	      _lastMouseWorldReleasePos = new Vector3();
	      _lastMouseScreenPos = new Vector3();

	      //Test units
	      squads = new Array<Array<Vector2>>();
	      units = new Array<Vector2>();
	      
	      //Test buildings
	      buildings = new Array<Building>();
	      
	      //Setup UI
	      stage = new Stage();
	      //Mouse POS
	      BitmapFont font = new BitmapFont();

	      textStyle = new LabelStyle();
	      textStyle.font = font;
	      mousePosLbl = new Label("", textStyle);
	      mousePosLbl.setPosition(10, 10);
	      stage.addActor(mousePosLbl);
	      
	      //Building button
	      Texture buildingTexture = new Texture("BuildingBtn.png");
	      Skin skin = new Skin();
	      skin.add("up", buildingTexture);
	      skin.add("down", buildingTexture);
	      skin.add("over", buildingTexture);
	      ImageTextButtonStyle buttonStyle = new ImageTextButtonStyle();
	      buttonStyle.up = skin.getDrawable("up");
	      buttonStyle.down = skin.getDrawable("down");
	      buttonStyle.over = skin.getDrawable("over");
	      buttonStyle.font = new BitmapFont();
	      buttonStyle.fontColor = Color.WHITE;
	      createBuildingBtn = new ImageTextButton("BUILDNG", buttonStyle);
	      createBuildingBtn.setPosition(10, 500-30);
	      //createBuildingBtn.setBounds(0, 0, buildingTexture.getWidth(),
	    	//	  buildingTexture.getHeight());
	      createBuildingBtn.addListener(new ClickListener() {
	            @Override
	            public void clicked(InputEvent event, float x, float y) {
	            	currentMode = SelectionMode.PlaceBuilding;
	            }
	            
	            @Override
	            public void enter(InputEvent event, float x, 
	            		float y, int pointer, Actor fromActor){
	            }
	        });

	      stage.addActor(createBuildingBtn);
	      
	      //Unit button
	      createUnitBtn = new ImageTextButton("UNIT", buttonStyle);
	      createUnitBtn.setPosition(110, 500-30);
	      //createBuildingBtn.setBounds(0, 0, buildingTexture.getWidth(),
	    	//	  buildingTexture.getHeight());
	      createUnitBtn.addListener(new ClickListener() {
	            @Override
	            public void clicked(InputEvent event, float x, float y) {
	            	currentMode = SelectionMode.UnitPlacing;
	            }
	            
	            @Override
	            public void enter(InputEvent event, float x, 
	            		float y, int pointer, Actor fromActor){
	            }
	        });
	      stage.addActor(createUnitBtn);

	      //Scroll buildings
	      Label buildingListLbl = new Label("BUILDINGLIST", textStyle);
	      buildingListLbl.setPosition(stage.getWidth()-buildingListLbl.getWidth(), stage.getHeight()-buildingListLbl.getHeight());
	      stage.addActor(buildingListLbl);
	      
	        buildingScrollTable = new Table();
	        
	        final ScrollPane buildScroller = new ScrollPane(buildingScrollTable);
	        
	        //table.add(scroller).fill().expand();
	        buildScroller.setSize(buildingListLbl.getWidth(), stage.getHeight()*0.5f-20);

	        buildScroller.setPosition(stage.getWidth()-buildScroller.getWidth(), stage.getHeight()-buildScroller.getHeight()-buildingListLbl.getHeight());
	        this.stage.addActor(buildScroller);
	      
	      //Scroll units  
		      Label unitListLbl = new Label("UNITLIST", textStyle);
		      unitListLbl.setPosition(stage.getWidth()-buildingListLbl.getWidth(), stage.getHeight()*.5f-unitListLbl.getHeight());
		      stage.addActor(unitListLbl);
		      
		        unitScrollTable = new Table();
		        
		        final ScrollPane unitScroller = new ScrollPane(unitScrollTable);
		        
		        //table.add(scroller).fill().expand();
		        unitScroller.setSize(buildingListLbl.getWidth(), stage.getHeight()*0.5f-20);

		        unitScroller.setPosition(stage.getWidth()-unitScroller.getWidth(), stage.getHeight()*.5f-unitScroller.getHeight()-unitListLbl.getHeight());
		        this.stage.addActor(unitScroller);	        
	      //Set input
	      InputMultiplexer input = new InputMultiplexer();
	      input.addProcessor(stage);
	      input.addProcessor(this);
	      Gdx.input.setInputProcessor(input);
	      
	      
	}

	@Override
	public void render () {
		//Update camera
	    camera.update();
	    stage.act(Gdx.graphics.getDeltaTime());
	    //Refresh
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		//Start shape rendering
		shapeRenderer.setProjectionMatrix(camera.combined);
		
		shapeRenderer.begin(ShapeType.Filled);
		
		//Draw simulation grid
		shapeRenderer.setColor(Color.TEAL);
		for(int i=0;i<=gridWidth+1;i++)
			shapeRenderer.rectLine(i-.5f, 0-1.5f, i-.5f, gridHeight-.5f, 0.05f);
		for(int i=-1;i<=gridHeight;i++)
			shapeRenderer.rectLine(0-.5f, i-.5f, gridWidth+.5f, i-.5f, 0.05f);
		
		//Draw buildings
		for(Building building : buildings){
			shapeRenderer.setColor(Color.TEAL);
			shapeRenderer.rect(building.p0.x-.5f, (gridHeight-building.p0.y)-.5f, 
					building.width, -building.height);
			//Draw building door
			shapeRenderer.setColor(Color.YELLOW);
			shapeRenderer.rect(building.p0.x+building.door.x-.5f, (gridHeight-(building.p0.y+building.door.y))-.5f, 
					1, -1);
		}		


		//Draw units at their position as circles
		shapeRenderer.setColor(Color.PINK);
		for(Array<Vector2> squad : squads){
			for(Vector2 unit : squad){
				shapeRenderer.circle(unit.x, unit.y, 0.4f, 10);
			}
		}
		Gdx.gl.glEnable(GL20.GL_BLEND);

		//Draw highlighted grid cell types
		switch (currentMode) {
        case Normal:
    		shapeRenderer.setColor(new Color(1,1,1,.4f));
        	break;
        case PlaceBuilding:
        	
        	//Draw building layout as mouse is dragged
    		shapeRenderer.setColor(new Color(1,0,0,0.4f));
    		
    		if(hoverGridPos!=null && buildingStart != null){
    			Vector2 start = new Vector2(buildingStart);
    			Vector2 end = new Vector2(hoverGridPos);
    			if( hoverGridPos.x < buildingStart.x){
    				start.x = hoverGridPos.x;
    				end.x = buildingStart.x;
    			}
    			if(hoverGridPos.y < buildingStart.y){
    				start.y = hoverGridPos.y;
    				end.y = buildingStart.y;    				
    			}
    			for(int i=(int) start.x;i<=end.x;i++){
        			for(int j=(int) start.y;j<=end.y;j++){
            			shapeRenderer.rect(i-.5f, (gridHeight-j)-1.5f, 
            					1, 1);
        			}
    			}
    		}
    		shapeRenderer.setColor(new Color(.95f,.95f,1,.4f));
        	break;
        case DoorPlacing:
        	//Draw planned building while user chooses door place
    		shapeRenderer.setColor(new Color(1,0,0,0.4f));
    		
    		if(hoverGridPos!=null && newBuilding != null){
    			Vector2 start = new Vector2(newBuilding.p0);
    			Vector2 end = new Vector2(newBuilding.p0.x+newBuilding.width,
    					newBuilding.p0.y+newBuilding.height);
    			for(int i=(int) start.x;i<end.x;i++){
        			for(int j=(int) start.y+1;j<=end.y;j++){
            			shapeRenderer.rect(i-.5f, (gridHeight-j)-.5f, 
            					1, 1);
        			}
    			}
    		}
    		shapeRenderer.setColor(new Color(0,0,1,.4f));
        	break;
        case UnitPlacing:
        	//Draw units being placed
        	Color selectionColor = new Color(Color.PINK);
        	selectionColor.a = 0.4f;
        	
    		shapeRenderer.setColor(selectionColor);
    		
    		if(hoverGridPos!=null){
    			if(hoverGridPos.x-1 >= 0 && hoverGridPos.x+1 < gridWidth &&
    					hoverGridPos.y-1 >= 0 && hoverGridPos.y+1 < gridHeight){
        			shapeRenderer.circle(hoverGridPos.x-1, gridHeight-hoverGridPos.y-2, 0.4f, 10);
        			shapeRenderer.circle(hoverGridPos.x-1, gridHeight-hoverGridPos.y, 0.4f, 10);
        			shapeRenderer.circle(hoverGridPos.x+1, gridHeight-hoverGridPos.y-2, 0.4f, 10);
        			shapeRenderer.circle(hoverGridPos.x+1, gridHeight-hoverGridPos.y, 0.4f, 10);

    			}
    		}
        	selectionColor.a = 0;
        	
    		shapeRenderer.setColor(selectionColor);
        	break;
		}

    		if(hoverGridPos !=null){
    			shapeRenderer.rect(hoverGridPos.x-.5f, (gridHeight-(hoverGridPos.y))-.5f, 
    					1, -1);
    		};
		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
		//Draw GUI
		stage.draw();
	}
	
	@Override
	public void resize(int width, int height) {
		float aspectRatio = (float) width / (float) height;
		Vector3 pos = camera.position;
		float zoom = camera.zoom;
        camera = new OrthographicCamera(CELL_SIZE * aspectRatio, CELL_SIZE);
        camera.position.set(pos);
        stage.getViewport().setScreenSize((int) (CELL_SIZE * aspectRatio), CELL_SIZE);
        camera.zoom = zoom;
        
        //Set UI redraw
        //OrthographicCamera stageCam = new OrthographicCamera(500*aspectRatio, 500);
        //stage.getViewport().setCamera(stageCam);
        stage.getViewport().update(width, height,true);
        //createBuildingBtn.setSize(80,20*(height/500));
        //stage.getViewport().getCamera().position.set(500/2,500/2,0);
	}
	
	@Override
	public boolean touchDown(int x, int y, int pointer, int button)
	{
	   _mouseButtonDown = button;
	   _lastMouseWorldPressPos.set(x, y, 0);
	   camera.unproject(_lastMouseWorldPressPos);
	   
	   if(currentMode == SelectionMode.PlaceBuilding && hoverGridPos !=null){
		   buildingStart = new Vector2(hoverGridPos);
	   }
	   return false;
	}
	

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
	   _mouseButtonDown = -1;
	   _lastMouseWorldReleasePos.set(x, y, 0);
	   camera.unproject(_lastMouseWorldReleasePos);
	   if(currentMode == SelectionMode.PlaceBuilding && buildingStart != null
			   && hoverGridPos != null){
		   Vector2 end =  new Vector2(hoverGridPos);
		   int temp = 0;
		   if(end.x < buildingStart.x){
			   temp = (int) buildingStart.x;
			   buildingStart.x = end.x;
			   end.x = temp;
		   }
		   if(end.y < buildingStart.y){
			   temp = (int) buildingStart.y;
			   buildingStart.y = end.y;
			   end.y = temp;			   
		   }
		   if(((int)(end.x-buildingStart.x)+1 >= 2 && (int)(end.y-buildingStart.y)+1 >=3) ||
		   ((int)(end.x-buildingStart.x)+1 >= 3 && (int)(end.y-buildingStart.y)+1 >=2)){
				newBuilding = new Building(buildingStart, (int)(end.x-buildingStart.x)+1,
						(int)(end.y-buildingStart.y)+1);
				   buildingStart = null;
				   currentMode = SelectionMode.DoorPlacing;
		   }else{
			   buildingStart = null;
			   //Warning here
		   }
		   

	   }
	   if(currentMode == SelectionMode.DoorPlacing && newBuilding != null
			   && hoverGridPos != null){
		   Vector2 doorPos = new Vector2(hoverGridPos);
		   if(doorPos.x >= newBuilding.p0.x && 
				   (doorPos.x > newBuilding.p0.x &&
					doorPos.x < newBuilding.p0.x + newBuilding.width-1 &&
				   (doorPos.y == newBuilding.p0.y ||
					doorPos.y == newBuilding.p0.y + newBuilding.height-1))||
				   (doorPos.y > newBuilding.p0.y &&
					doorPos.y < newBuilding.p0.y + newBuilding.height-1 &&
					(doorPos.x== newBuilding.p0.x ||
					doorPos.x == newBuilding.p0.x + newBuilding.width-1))){
			  newBuilding.door = new Vector2(doorPos.x-newBuilding.p0.x,
					  doorPos.y-newBuilding.p0.y);
			  buildings.add(newBuilding);
		      Label text = new Label(""+buildings.size, textStyle);
		      text.setAlignment(Align.left);
		      //Building button
		      Texture buildingTexture = new Texture("Delete.png");
		      Skin skin = new Skin();
		      skin.add("up", buildingTexture);
		      skin.add("down", buildingTexture);
		      skin.add("over", buildingTexture);
		      ImageButtonStyle buttonStyle = new ImageButtonStyle();
		      buttonStyle.up = skin.getDrawable("up");
		      buttonStyle.down = skin.getDrawable("down");
		      buttonStyle.over = skin.getDrawable("over");
		      ImageButton createBuildingBtn = new ImageButton(buttonStyle);
		      Table table = new Table();
		      table.add(createBuildingBtn).size(10, 10);
		      final Table rowTable = new Table();
		      //createBuildingBtn.setPosition(10, 500-30);
		      //createBuildingBtn.setBounds(0, 0, buildingTexture.getWidth(),
		    	//	  buildingTexture.getHeight());
		      createBuildingBtn.addListener(new ClickListener() {
		            
		            Building storedBuilding = newBuilding;
		            Table removedTable = rowTable;
		            @Override
		            public void clicked(InputEvent event, float x, float y) {
		            	buildings.removeValue(storedBuilding, false);
		            	buildingScrollTable.removeActor(rowTable);
		            }
		            
		            @Override
		            public void enter(InputEvent event, float x, 
		            		float y, int pointer, Actor fromActor){
		            }
		        });
		      rowTable.add(text);
		      rowTable.add(table);
		      buildingScrollTable.add(rowTable);
		      buildingScrollTable.row();
		      buildingScrollTable.align(Align.top);
			  newBuilding = null;
			  currentMode = SelectionMode.Normal;

		   }
	   }
		if(currentMode == SelectionMode.UnitPlacing && hoverGridPos!=null){
			if(hoverGridPos.x-1 >= 0 && hoverGridPos.x+1 < gridWidth &&
					hoverGridPos.y-1 >= 0 && hoverGridPos.y+1 < gridHeight){
				final Array<Vector2> units = new Array<Vector2>();
			      units.add(new Vector2(hoverGridPos.x-1, gridHeight-hoverGridPos.y-2));
			      units.add(new Vector2(hoverGridPos.x-1, gridHeight-hoverGridPos.y));
			      units.add(new Vector2(hoverGridPos.x+1, gridHeight-hoverGridPos.y-2));
			      units.add(new Vector2(hoverGridPos.x+1, gridHeight-hoverGridPos.y));
			      squads.add(units);
			      currentMode = SelectionMode.Normal;
			      Label text = new Label(""+squads.size, textStyle);
			      text.setAlignment(Align.left);
			      //Building button
			      Texture buildingTexture = new Texture("Delete.png");
			      Skin skin = new Skin();
			      skin.add("up", buildingTexture);
			      skin.add("down", buildingTexture);
			      skin.add("over", buildingTexture);
			      ImageButtonStyle buttonStyle = new ImageButtonStyle();
			      buttonStyle.up = skin.getDrawable("up");
			      buttonStyle.down = skin.getDrawable("down");
			      buttonStyle.over = skin.getDrawable("over");
			      ImageButton createBuildingBtn = new ImageButton(buttonStyle);
			      Table table = new Table();
			      table.add(createBuildingBtn).size(10, 10);
			      final Table rowTable = new Table();
			      //createBuildingBtn.setPosition(10, 500-30);
			      //createBuildingBtn.setBounds(0, 0, buildingTexture.getWidth(),
			    	//	  buildingTexture.getHeight());
			      createBuildingBtn.addListener(new ClickListener() {
			            
			            Array<Vector2> storedSquad = units;
			            Table removedTable = rowTable;
			            @Override
			            public void clicked(InputEvent event, float x, float y) {
			            	squads.removeValue(units, false);
			            	unitScrollTable.removeActor(rowTable);
			            }
			            
			            @Override
			            public void enter(InputEvent event, float x, 
			            		float y, int pointer, Actor fromActor){
			            }
			        });
			      rowTable.add(text);
			      rowTable.add(table);
			      unitScrollTable.add(rowTable);
			      unitScrollTable.row();
			      unitScrollTable.align(Align.top);
			}
		}
	   return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
	
	   if (_mouseButtonDown == Input.Buttons.RIGHT)
	   {
		   camera.translate((x-_lastMouseScreenPos.x)/CELL_SIZE,
	                        (y-_lastMouseScreenPos.y)/-CELL_SIZE);
	   }
	   _lastMouseWorldDragPos.set(x, y, 0);
	   camera.unproject(_lastMouseWorldDragPos);
	   _lastMouseWorldMovePos.set(x,y,0);
	   camera.unproject(_lastMouseWorldMovePos);
	   _lastMouseScreenPos.set(x,y,0);
	   int gridX = (int)Math.floor(_lastMouseWorldMovePos.x+0.5);
	   int gridY = (int)Math.floor(gridHeight-_lastMouseWorldMovePos.y-0.5);
	   if(gridX < 0 || gridX > gridWidth || gridY < 0 || gridY > gridHeight){
		   mousePosLbl.setText("");
		   hoverGridPos = null;
	   }
	   else{
		   mousePosLbl.setText("GridPos ("+gridX+","+
			   gridY+")");
		   hoverGridPos = new Vector2(gridX,gridY);
	   }
	   return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int x, int y) {
		   _lastMouseWorldMovePos.set(x, y, 0);
		   camera.unproject(_lastMouseWorldMovePos);
		   _lastMouseScreenPos.set(x,y,0);
		   int gridX = (int)Math.floor(_lastMouseWorldMovePos.x+0.5);
		   int gridY = (int)Math.floor(gridHeight-_lastMouseWorldMovePos.y-0.5);
		   if(gridX < 0 || gridX > gridWidth || gridY < 0 || gridY > gridHeight){
			   mousePosLbl.setText("");
			   hoverGridPos = null;
		   }
		   else{
			   mousePosLbl.setText("GridPos ("+gridX+","+
				   gridY+")");
			   hoverGridPos = new Vector2(gridX,gridY);
		   }
		   return false;
	}

	@Override
	public boolean scrolled(int amount) {
		camera.zoom+=amount*0.05f;
		return false;
	}
}