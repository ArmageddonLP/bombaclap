package config;

/**
 * config.Constants which are used across all files. Helps to manage constants when they're centralized
 */
public class Constants {
    public static String resourcePath = System.getProperty("user.dir") + "/assets/textures/";

    public static String solidBlock = resourcePath + "solid_block.png";
    public static String brittleBlock = resourcePath + "brittle_block.png";
    public static String sandBlock = resourcePath + "sand_block.png";
    public static String grassBlock = resourcePath + "grass_block.png";
    public static String grassBlockFire = resourcePath + "grass_block_fire.png";
    public static String destroyedWall = resourcePath + "destroyedfield.png";
    public static String burnedSand = resourcePath + "burnedsand.png";

    public static String playerOne = resourcePath + "player1.png";
    public static String playerTwo = resourcePath + "player2.png";
    public static String playerThree = resourcePath + "player3.png";
    public static String playerFour = resourcePath + "player4.png";

    public static String nothing = resourcePath + "nothing.png";
    public static String bombPlacedBlack = resourcePath + "bomb_placed.png";
    public static String bombPlacedRed = resourcePath + "bomb_placed_red.png";
    public static String bombExplode = resourcePath + "bomb_explode.png";
    public static String fireFromExplosion = resourcePath + "fire.png";

    public static String musicFolder = "assets/music";

    public static String timerTextId = "timerText";
    public static String timerBackId = "timerBack";

    public static int blockSize = 64; // in px
    public static int blocksPerLine = 15; // 15 blocks
    public static int borderWidth = 10; // in px
    public static int mapWidth = blockSize * blocksPerLine + 2 * borderWidth;
}
