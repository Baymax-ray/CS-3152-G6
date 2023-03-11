package edu.cornell.gdiac.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.GameCanvas;

public class Level {

    //#region FINAL FIELDS

    private final Player player;
    private final Enemy[] enemies;


    // NOTE: the natural way of viewing a 2d array is flipped for the map. tilemap[0] is the top of the map. need fancy
    // conversions between the two spaces
    private final int[][] tilemap; // value represents the index of the tile in the tiles array

    private final Tile[] tiles;

    private final BodyDef bodyDef;
    private final FixtureDef fixtureDef;

//    private final TextureRegion backgroundTexture;

    /**
     * In level coordinates (not pixels), the width and height of a single tile.
     */
    private final float tileSize;

    /**
     * In level coordinates, the width of the region the camera displays
     */
    private final float cameraWidth;

    /**
     * In level coordinates, the height of the region the camera displays
     */
    private final float cameraHeight;

    private final float gravity;

    //#endregion


    //#region NONFINAL FIELDS

    /**
     * Whether this level has been completed
     */
    private boolean isCompleted;

    /**
     * The x position of the camera in level coordinates
     */
    private float cameraX;

    /**
     * The y position of the camera in level coordinates
     */
    private float cameraY;

    //#endregion

    //#region GETTERS & SETTERS

    public float getGravity() {
        return gravity;
    }

    public Enemy[] getEnemies() {
        return enemies;
    }

    //#endregion

    /**
     * presumably useful classes for AI
     *
     * @param x the x coordinate of the point in level coordinates
     * @param y the y coordinate of the point in level coordinates
     * @return
     */
    public Tile tileAt(float x, float y) {
        return tiles[tilemap[levelToTileCoordinatesX(x)][levelToTileCoordinatesY(y)]];
    }

    /**
     * presumably useful classes for AI
     *
     * @param x the x coordinate of the point in level coordinates
     * @param y the y coordinate of the point in level coordinates
     * @return whether the point is in an air tile.
     */
    public boolean isAirAt(float x, float y) {
        return tilemap[levelToTileCoordinatesX(x)][levelToTileCoordinatesY(y)] < 0;
    }


    public int levelToTileCoordinatesX(float x) {
        return (int) Math.floor(x / this.tileSize);
    }

    public int levelToTileCoordinatesY(float y) {
        return tilemap.length - 1 - (int) Math.floor(y / this.tileSize);
    }

    public float tileToLevelCoordinatesX(int x) {
        return x * this.tileSize;
    }

    public float tileToLevelCoordinatesY(int y) {
        return (tilemap.length - 1 - y) * this.tileSize;
    }




    public void draw(GameCanvas canvas) {
        canvas.getCamera().position.setZero(); // set to some other position to follow player;
        canvas.getCamera().setToOrtho(false, cameraWidth, cameraHeight);
//        canvas.draw(backgroundTexture, 0, 0);

        for (int y = 0; y < tilemap.length; y++) {
            int[] row = tilemap[y];
            for (int x = 0; x < tilemap.length; x++) {
                int tileId = row[x];
                if (tileId < 0) continue;
                Tile tile = tiles[tileId];
                canvas.draw(tile.getTexture(), Color.WHITE, 0, 0, tileToLevelCoordinatesX(x), tileToLevelCoordinatesY(y), 0, tileSize / tile.getTexture().getRegionWidth(), tileSize / tile.getTexture().getRegionHeight());
            }
        }

        player.draw(canvas);
    }

    public void activatePhysics(World world) {
        for (int y = 0; y < tilemap.length; y++) {
            int[] row = tilemap[y];
            for (int x = 0; x < tilemap.length; x++) {
                int tileId = row[x];
                if (tileId < 0) continue;
                Tile tile = tiles[tileId];
                bodyDef.position.x = tileToLevelCoordinatesX(x);
                bodyDef.position.y = tileToLevelCoordinatesY(y);
                bodyDef.active = true;
                Body body = world.createBody(bodyDef);
                body.setUserData(tile);

                PolygonShape shape = new PolygonShape();
                shape.set(new float[]{
                        0,        0,
                        tileSize, 0,
                        tileSize, tileSize,
                        0,        tileSize,
                });

                fixtureDef.shape = shape;
                body.createFixture(fixtureDef);
            }
        }
        player.activatePhysics(world);
        //TODO: enemies activate too
    }


    public Level(JsonValue json, Tile[] tiles, AssetDirectory assets) {
        this.tiles = tiles;

        int widthInTiles = json.getInt("widthInTiles");
        int heightInTiles = json.getInt("heightInTiles");

        this.tilemap = new int[heightInTiles][widthInTiles];
        for (int y = 0; y < heightInTiles; y++) {
            JsonValue row = json.get("tilemap").get(y);
            for (int x = 0; x < widthInTiles; x++) {
                tilemap[y][x] = row.getInt(x);
            }
        }

        this.tileSize = json.getFloat("tileSize");
        this.cameraWidth = json.getFloat("cameraWidth");
        this.cameraHeight = json.getFloat("cameraHeight");
        this.gravity = json.getFloat("gravity");


//        String backgroundAsset = json.getString("backgroundAsset");
//        this.backgroundTexture = assets.get(backgroundAsset);


        this.player = new Player(json.get("player"), assets);

        // TODO: need more than 0, ideally
        this.enemies = new Enemy[0];

        this.bodyDef = new BodyDef();
        this.bodyDef.type = BodyDef.BodyType.StaticBody;
        this.bodyDef.active = false;

        this.fixtureDef = new FixtureDef();
    }

}