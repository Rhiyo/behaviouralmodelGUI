package com.behaviouralmodel.gui;

import behaviouralmodel.Building;
import behaviouralmodel.HTN;
import behaviouralmodel.Unit;
import behaviouralmodel.UnitMember;

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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.behaviouralmodel.*;
public class BMGui extends ApplicationAdapter implements InputProcessor {
	
	public static enum SelectionMode{
		Normal, PlaceBuilding, DoorPlacing, UnitPlacing, Popup
	}
	
	//The size of the simulation
	int gridWidth;
	int gridHeight;
	SelectionMode currentMode = SelectionMode.Normal;
	SelectionMode beforePopup;
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
	
	HTN htn;
	
	//Create stage for UI
	Stage stage;
	Label mousePosLbl;
	ImageTextButton createBuildingBtn;
	ImageTextButton createUnitBtn;
	Table buildingScrollTable;
	Table unitScrollTable;
	Table commandScrollTable;
    LabelStyle textStyle;
    ImageTextButtonStyle buttonStyle;
    Table popupTable;
    
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
	      
	      htn = new HTN();
	      
	      //Setup UI
	      stage = new Stage();
	      
	      textStyle = new LabelStyle();
	      BitmapFont font = new BitmapFont();
	      textStyle.font = font;
	      
	      Texture btnTexture = new Texture("BuildingBtn.png");
	      Texture btnDownTexture = new Texture("BuildingBtnDown.png");
	      Skin skin = new Skin();
	      skin.add("up", btnTexture);
	      skin.add("down", btnDownTexture);
	      skin.add("over", btnTexture);
	      buttonStyle = new ImageTextButtonStyle();
	      buttonStyle.up = skin.getDrawable("up");
	      buttonStyle.down = skin.getDrawable("down");
	      buttonStyle.over = skin.getDrawable("over");
	      buttonStyle.font = new BitmapFont();
	      buttonStyle.fontColor = Color.WHITE;
	      buttonStyle.downFontColor = Color.RED;
	      //Root table
	      Table rootTable = new Table();
	      rootTable.setFillParent(true);
	      
	      Stack stack = new Stack();
	      popupTable = new Table();
	      Table guiTable = new Table();
	      //scroll Commands
	      guiTable.add(createCommandPane()).size(Gdx.graphics.getWidth()*0.2f,
	    		  Gdx.graphics.getHeight()*0.5f);
	      
	      //Buttons
	      Table createButtons = new Table();
	      createButtons.top();
	      //Building button
	      createButtons.add(createBuildingButton()).pad(10);
	      //Unit button
	      createButtons.add(createUnitButton()).pad(10);	      
	      
	      guiTable.add(createButtons).size(stage.getWidth()*0.6f,
	    		  Gdx.graphics.getHeight()*0.5f);
	      
	      //Scroll buildings
	      guiTable.add(createBuildingPane()).size(stage.getWidth()*0.2f,
	    		  stage.getHeight()*0.5f);
	      
	      
	      guiTable.row();
	      
	      //Filler, command buttons later
	      Table commandBtnTable = new Table();
	      commandBtnTable.top();
	      commandBtnTable.add(createMoveCommandButton());
	      guiTable.add(commandBtnTable).size(stage.getWidth()*0.2f,
	    		  stage.getHeight()*0.5f);
	      
	      //Mouse POS
	      Table textTable = new Table();
	      textTable.bottom();
	      textTable.add(createGridPosLabel()).pad(10);
	      guiTable.add(textTable).size(stage.getWidth()*0.6f,
	    		  stage.getHeight()*0.5f);
	      
	      //Scroll units  
	      guiTable.add(createUnitPane()).size(stage.getWidth()*0.2f,
	    		  stage.getHeight()*0.5f);
	      
	      stack.add(guiTable);
	      stack.add(popupTable);
	      rootTable.add(stack);
	      stage.addActor(rootTable);
	      
		        
		        
		  

	      //Set input
	      InputMultiplexer input = new InputMultiplexer();
	      input.addProcessor(stage);
	      input.addProcessor(this);
	      Gdx.input.setInputProcessor(input);
	      
	      
	}

	@Override
	public void render () {
		//Update HTN
		htn.Update(Gdx.graphics.getDeltaTime());
		
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
		for(Building building : htn.GetBuildings()){
			shapeRenderer.setColor(Color.TEAL);
			shapeRenderer.rect(building.getX() -.5f, (gridHeight-building.getY())-.5f, 
					building.getWidth(), -building.getHeight());
			//Draw building door
			shapeRenderer.setColor(Color.YELLOW);
			shapeRenderer.rect(building.getDoorX()-.5f, (gridHeight-(building.getDoorY()))-.5f, 
					1, -1);
		}		


		//Draw units at their position as circles
		shapeRenderer.setColor(Color.PINK);
		for(Unit unit : htn.GetUnits()){
			for(UnitMember unitMember : unit.GetUnitMembers()){
				shapeRenderer.circle(unit.getX()+unitMember.getX(), gridHeight-1-unit.getY()+unitMember.getY(), 0.4f, 10);
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
    			Vector2 start = new Vector2(newBuilding.getX(),newBuilding.getY());
    			Vector2 end = new Vector2(newBuilding.getX()+newBuilding.getWidth(),
    					newBuilding.getY()+newBuilding.getHeight());
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
	   
	   if (button == Input.Buttons.LEFT){
	   if(currentMode == SelectionMode.PlaceBuilding && hoverGridPos !=null){
		   buildingStart = new Vector2(hoverGridPos);
	   }
	   }
	   return false;
	}
	

	@Override
	public boolean touchUp(int x, int y, int pointer, int button)
	{
	   
	   _lastMouseWorldReleasePos.set(x, y, 0);
	   camera.unproject(_lastMouseWorldReleasePos);
	   if (_mouseButtonDown == Input.Buttons.LEFT){
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
				newBuilding = new Building((int)buildingStart.x, (int)buildingStart.y, (int)(end.x-buildingStart.x)+1,
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
		   if(doorPos.x >= newBuilding.getX() && 
				   (doorPos.x > newBuilding.getX() &&
					doorPos.x < newBuilding.getX() + newBuilding.getWidth()-1 &&
				   (doorPos.y == newBuilding.getY() ||
					doorPos.y == newBuilding.getY() + newBuilding.getHeight()-1))||
				   (doorPos.y > newBuilding.getY() &&
					doorPos.y < newBuilding.getY() + newBuilding.getHeight()-1 &&
					(doorPos.x== newBuilding.getX() ||
					doorPos.x == newBuilding.getX() + newBuilding.getWidth()-1))){
			  newBuilding.setDoor((int)doorPos.x, (int)doorPos.y);
			  popupTable.add(createStringInput("Building ID", "building"+htn.GetBuildings().size(),
					  new AcceptListener() {
		            @Override
		            public void clicked(InputEvent event, float x, float y) {
		            	newBuilding.setId(textToSteal.getText());
		            	htn.GetBuildings().add(newBuilding);
		  		      Label text = new Label(textToSteal.getText(), textStyle);
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
		  		            	htn.GetBuildings().remove(storedBuilding);
		  		            	buildingScrollTable.removeActor(rowTable);
		  		            }

		  		        });
		  		      rowTable.add(text);
		  		      rowTable.add(table);
		  		      buildingScrollTable.add(rowTable);
		  		      buildingScrollTable.row();
		  		      buildingScrollTable.align(Align.top);
		  			  newBuilding = null;
		  			  popupTable.clear();
		  			  currentMode = SelectionMode.PlaceBuilding;
		            }
		            
		        })).size(stage.getWidth()*0.4f,
					  stage.getHeight()*0.25f);
			  
			  beforePopup = SelectionMode.PlaceBuilding;
			  currentMode = SelectionMode.Popup;

		   }
	   }
		if(currentMode == SelectionMode.UnitPlacing && hoverGridPos!=null){
			if(hoverGridPos.x-1 >= 0 && hoverGridPos.x+1 < gridWidth &&
					hoverGridPos.y-1 >= 0 && hoverGridPos.y+1 < gridHeight){
				//final Array<Vector2> units = new Array<Vector2>();
			      //units.add(new Vector2(hoverGridPos.x-1, gridHeight-hoverGridPos.y-2));
			      //units.add(new Vector2(hoverGridPos.x-1, gridHeight-hoverGridPos.y));
			      //units.add(new Vector2(hoverGridPos.x+1, gridHeight-hoverGridPos.y-2));
			      //units.add(new Vector2(hoverGridPos.x+1, gridHeight-hoverGridPos.y));
				final Unit newUnit = new Unit((int)hoverGridPos.x, (int)hoverGridPos.y);
				beforePopup = currentMode;
				currentMode = SelectionMode.Popup;
				  popupTable.add(createStringInput("Unit ID", "unit"+htn.GetUnits().size(),
						  new AcceptListener() {
			            @Override
			            public void clicked(InputEvent event, float x, float y) {
			            	Label text = new Label(textToSteal.getText(), textStyle);
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
						            
						            Unit storedUnit = newUnit;
						            Table removedTable = rowTable;
						            @Override
						            public void clicked(InputEvent event, float x, float y) {
						            	htn.GetUnits().remove(newUnit);
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
						      newUnit.setId(textToSteal.getText());
						      htn.GetUnits().add(newUnit);
						      popupTable.clear();
						      currentMode = SelectionMode.UnitPlacing;
			            }
				  })).size(stage.getWidth()*0.4f,stage.getHeight()*0.25f);
			      
			}
		}
		_mouseButtonDown = -1;
		}
	   return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer)
	{
	
	   if (_mouseButtonDown == Input.Buttons.MIDDLE)
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
	
	/*
	 * Create stage widgets
	 */
	
	//Building list
	private Table createBuildingPane(){
		Table table = new Table();
		table.top();
	      Label buildingListLbl = new Label("BUILDINGS", textStyle);
	      buildingListLbl.setPosition(stage.getWidth()-buildingListLbl.getWidth(), stage.getHeight()-buildingListLbl.getHeight());
	      table.add(buildingListLbl);
	      
	        buildingScrollTable = new Table();
	        
	        final ScrollPane buildScroller = new ScrollPane(buildingScrollTable);
	        
	        //table.add(scroller).fill().expand();

	        table.row();
	        table.add(buildScroller);
	        return table;
	}
	
	//Unit List
	private Table createUnitPane(){
		Table table = new Table();
		table.top();
	      Label unitListLbl = new Label("UNITS", textStyle);
	      unitListLbl.setPosition(stage.getWidth()-unitListLbl.getWidth(), stage.getHeight()*.5f-unitListLbl.getHeight());
	      table.add(unitListLbl);
	      
	        unitScrollTable = new Table();
	        
	        final ScrollPane unitScroller = new ScrollPane(unitScrollTable);
	        
	        //table.add(scroller).fill().expand();
	        unitScroller.setSize(unitListLbl.getWidth(), stage.getHeight()*0.5f-20);

	        unitScroller.setPosition(stage.getWidth()-unitScroller.getWidth(), stage.getHeight()*.5f-unitScroller.getHeight()-unitListLbl.getHeight());
	        table.row();
	        table.add(unitScrollTable);
	        return table;
	}
	
	//Command list
	private Table createCommandPane(){
		Table table = new Table();
		table.top();
        Label commandListLbl = new Label("COMMANDS", textStyle);
	      table.add(commandListLbl);
	      	
	        commandScrollTable = new Table();
	        
	        final ScrollPane commandScroller = new ScrollPane(commandScrollTable);

	        table.row();
	        table.add(commandScrollTable);
	    
	    return table;
	}
	
	//New building button
	private ImageTextButton createBuildingButton(){
	      createBuildingBtn = new ImageTextButton("BUILDING", buttonStyle);
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

	      return createBuildingBtn;
	}
	
	//New unit button
	
	private ImageTextButton createUnitButton(){
	      createUnitBtn = new ImageTextButton("UNIT", buttonStyle);
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
	      return createUnitBtn;
	}
	
	//Move command button
	private ImageTextButton createMoveCommandButton(){
	      ImageTextButton button = new ImageTextButton("MOVE", buttonStyle);
	      //createBuildingBtn.setBounds(0, 0, buildingTexture.getWidth(),
	    	//	  buildingTexture.getHeight());
	      createUnitBtn.addListener(new ClickListener() {
	            @Override
	            public void clicked(InputEvent event, float x, float y) {
	            }
	            
	        });
	      return button;
	}
	
	//Grid position label
	private Label createGridPosLabel(){
	      mousePosLbl = new Label("", textStyle);
	      return mousePosLbl;
	}
	
	//String input popup
	private Table createStringInput(String message, String defaultTxt, AcceptListener acceptListener){
		Table table = new Table();
		TextureRegionDrawable drawable = new TextureRegionDrawable();
		drawable.setRegion(new TextureRegion(new Texture("BuildingBtn.png")));
		table.background(drawable);
        Skin textboxskin = new Skin();
        //textboxskin.add("cursor", new Texture("data/cursortextfield.png"));
        //textboxskin.add("selection", new Texture("data/selection.png"));
        textboxskin.add("font", new BitmapFont());

        TextFieldStyle textfieldstyle = new TextFieldStyle();
        textfieldstyle.disabledFontColor=Color.BLACK;
        textfieldstyle.font=textboxskin.getFont("font");
        textfieldstyle.fontColor=Color.WHITE;
        //textfieldstyle.cursor=textboxskin.getDrawable("cursor");
        //textfieldstyle.selection=textboxskin.getDrawable("selection"); 
		TextField textfield = new TextField(defaultTxt, textfieldstyle);
		Table tfTable = new Table();
		TextureRegionDrawable tfDrawable = new TextureRegionDrawable();

		tfDrawable.setRegion(new TextureRegion(new Texture("InputBG.png")));
		tfTable.setBackground(tfDrawable);
		tfTable.add(textfield).pad(7);
		Label msgLbl = new Label(message, textStyle);
		table.add(msgLbl);
		table.row();
		table.add(tfTable);
		table.row();
		Table buttonRow = new Table();
		ImageTextButton cancelBtn = new ImageTextButton("CANCEL", buttonStyle);
		cancelBtn.addListener(new ClickListener() {
		            @Override
		            public void clicked(InputEvent event, float x, float y) {
		            	currentMode = beforePopup;
		            	popupTable.clear();
		            }
		            
		        });
		buttonRow.add(cancelBtn).pad(10);
		ImageTextButton acceptBtn = new ImageTextButton("OK", buttonStyle);
		acceptListener.textToSteal = textfield;
		acceptBtn.addListener(acceptListener);
		buttonRow.add(acceptBtn).pad(10);
		table.add(buttonRow);
		return table;
	}
	
	//Selection input popup
	private Table createSelectionInput(String message, String defaultTxt, AcceptListener acceptListener){
		Table table = new Table();
		TextureRegionDrawable drawable = new TextureRegionDrawable();
		drawable.setRegion(new TextureRegion(new Texture("BuildingBtn.png")));
		table.background(drawable);
        Skin textboxskin = new Skin();
        //textboxskin.add("cursor", new Texture("data/cursortextfield.png"));
        //textboxskin.add("selection", new Texture("data/selection.png"));
        textboxskin.add("font", new BitmapFont());

        TextFieldStyle textfieldstyle = new TextFieldStyle();
        textfieldstyle.disabledFontColor=Color.BLACK;
        textfieldstyle.font=textboxskin.getFont("font");
        textfieldstyle.fontColor=Color.WHITE;
        //textfieldstyle.cursor=textboxskin.getDrawable("cursor");
        //textfieldstyle.selection=textboxskin.getDrawable("selection"); 
		TextField textfield = new TextField(defaultTxt, textfieldstyle);
		Table tfTable = new Table();
		TextureRegionDrawable tfDrawable = new TextureRegionDrawable();

		tfDrawable.setRegion(new TextureRegion(new Texture("InputBG.png")));
		tfTable.setBackground(tfDrawable);
		tfTable.add(textfield).pad(7);
		Label msgLbl = new Label(message, textStyle);
		table.add(msgLbl);
		table.row();
		table.add(tfTable);
		table.row();
		Table buttonRow = new Table();
		ImageTextButton cancelBtn = new ImageTextButton("CANCEL", buttonStyle);
		cancelBtn.addListener(new ClickListener() {
		            @Override
		            public void clicked(InputEvent event, float x, float y) {
		            	currentMode = beforePopup;
		            	popupTable.clear();
		            }
		            
		        });
		buttonRow.add(cancelBtn).pad(10);
		ImageTextButton acceptBtn = new ImageTextButton("OK", buttonStyle);
		acceptListener.textToSteal = textfield;
		acceptBtn.addListener(acceptListener);
		buttonRow.add(acceptBtn).pad(10);
		table.add(buttonRow);
		return table;
	}
	
	/*
	 * Listener for popups
	 */
	
	private class AcceptListener extends ClickListener{
		public TextField textToSteal;
	}
	
}