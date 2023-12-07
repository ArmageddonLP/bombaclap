package models;

import config.Constants;

/**
 * ActionToken-Model used for regulating playerActions
 */
public class ActionToken {
    private long moveCooldown = 0;
    private long bombCooldown = 0;

    /**
     * Reduces all cooldowns of this actionToken according to the given tickDuration
     *
     * @param tickDuration amount to reduce the cooldowns by
     */
    public void reduceCooldowns(long tickDuration) {
        moveCooldown -= tickDuration;
        bombCooldown -= tickDuration;
        if (moveCooldown < 0) {
            moveCooldown = 0;
        }
        if (bombCooldown < 0) {
            bombCooldown = 0;
        }
    }

    /**
     * Restarts movement-cooldown if it is off cooldown
     *
     * @return true if movement is off cooldown, else false
     */
    public boolean move() {
        if (moveCooldown == 0) {
            moveCooldown = Constants.PLAYER_MOVEMENT_COOLDOWN;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Restarts bomb-cooldown if it is off cooldown
     *
     * @return true if bomb is off cooldown, else false
     */
    public boolean placeBomb() {
        if (bombCooldown == 0) {
            bombCooldown = Constants.PLAYER_BOMB_COOLDOWN;
            return true;
        } else {
            return false;
        }
    }
}
