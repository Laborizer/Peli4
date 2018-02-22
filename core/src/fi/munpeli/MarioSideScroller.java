package fi.munpeli;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class MarioSideScroller extends ApplicationAdapter {
	SpriteBatch batch;
	OrthographicCamera camera;
	TiledMap tiledMap;
	TiledMapRenderer tiledMapRenderer;
	Sprite player;
	private Sound coin;
	private Sound death;
	private Music bgm;
	float playerSpeed = 60f;
	boolean playerIsAlive = true;
	boolean deathsounded = true;
	boolean coinSoundActive = false;
	int coinCooldown = 30;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		player = new Sprite(new Texture(("ball.png")));
		player.setX(40f);
		player.setY(150f);

        coin = Gdx.audio.newSound(Gdx.files.internal("smw_coin.wav"));
        death = Gdx.audio.newSound(Gdx.files.internal("smb3_player_down.wav"));
        bgm = Gdx.audio.newMusic(Gdx.files.internal("athletic.mp3"));
        bgm.setLooping(true);
        bgm.play();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 400, 240);

		tiledMap = new TmxMapLoader().load("SMG3.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        cameraMove();

	}

	@Override
	public void render () {
        camera.update();
		batch.setProjectionMatrix(camera.combined);
		tiledMapRenderer.setView(camera);

		if(!playerIsAlive && deathsounded) {
		    deathsounded = false;
		    bgm.stop();
		    death.play();
        }

        if(coinSoundActive) {
		    coinCooldown--;
		    if (coinCooldown == 0) {
		        coinSoundActive = false;
		        coinCooldown = 30;
            }
        }

		checkCollisions();
		checkWallsCollisions();
		checkWorldWallsCollisions();
		playerMove();
		cameraMove();

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        tiledMapRenderer.render();
		batch.begin();
        if (playerIsAlive) {
            player.draw(batch);
        }
		batch.end();
	}

	public void playerMove() {
	    if (playerIsAlive) {
            player.setX(player.getX() + playerSpeed * Gdx.graphics.getDeltaTime());
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                player.setY(player.getY() + playerSpeed  * Gdx.graphics.getDeltaTime() * 3f);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                player.setY(player.getY() - playerSpeed * Gdx.graphics.getDeltaTime() * 3f);
            }

            Gdx.app.log("TAG", "X: " + player.getX());
            Gdx.app.log("TAG", "Properties " + tiledMap.getProperties());
        }
    }

    public void cameraMove() {
	    camera.position.set(player.getX(), player.getY(), 0);
        if(camera.position.x < 400 / 2){
            camera.position.x = 400 / 2;
        }

        if(camera.position.y > 320 - 240 / 2) {
            camera.position.y = 320 - 240 / 2;
        }

        if(camera.position.y < 240 / 2) {
            camera.position.y = 240 / 2;
        }

        if(camera.position.x > 1600 - 400 / 2f) {
            camera.position.x = 1600 - 400 / 2f;
        }
    }

    public void checkCollisions() {

        MapLayer collisionObjectLayer = (MapLayer)tiledMap.getLayers().get("coinwalls");

        MapObjects mapObjects = collisionObjectLayer.getObjects();

        Array<RectangleMapObject> rectangleObjects = mapObjects.getByType(RectangleMapObject.class);

        for (RectangleMapObject rectangleObject : rectangleObjects) {
            Rectangle rectangle = rectangleObject.getRectangle();
            if (player.getBoundingRectangle().overlaps(rectangle)) {
                clearObject(rectangle.getX(), rectangle.getY());
            }
        }
    }

    public void checkWallsCollisions() {

        MapLayer collisionObjectLayer = (MapLayer)tiledMap.getLayers().get("otherwalls");

        MapObjects mapObjects = collisionObjectLayer.getObjects();

        Array<RectangleMapObject> rectangleObjects = mapObjects.getByType(RectangleMapObject.class);

        for (RectangleMapObject rectangleObject : rectangleObjects) {
            Rectangle rectangle = rectangleObject.getRectangle();
            if (player.getBoundingRectangle().overlaps(rectangle)) {
                playerIsAlive = false;
            }
        }
    }

    public void checkWorldWallsCollisions() {

        MapLayer collisionObjectLayer = (MapLayer)tiledMap.getLayers().get("worldwalls");

        MapObjects mapObjects = collisionObjectLayer.getObjects();

        Array<RectangleMapObject> rectangleObjects = mapObjects.getByType(RectangleMapObject.class);

        for (RectangleMapObject rectangleObject : rectangleObjects) {
            Rectangle rectangle = rectangleObject.getRectangle();
            if (player.getBoundingRectangle().overlaps(rectangle)) {
                playerIsAlive = false;
            }
        }
    }

    public void clearObject(float xCoord, float yCoord) {
        int indexX = (int) xCoord / 16;
        int indexY = (int) yCoord / 16;

        TiledMapTileLayer wallCells = (TiledMapTileLayer) tiledMap.getLayers().get("COins");
        wallCells.setCell(indexX, indexY, null);
        if(!coinSoundActive) {
            coin.play();
            coinSoundActive = true;
        }
    }
	
	@Override
	public void dispose () {
		batch.dispose();
		tiledMap.dispose();
		death.dispose();
		coin.dispose();
		bgm.dispose();
	}
}
