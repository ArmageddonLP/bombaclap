package models;

import lombok.*;
import proxy.enums.PlayerColor;
import proxy.enums.PlayerDirection;

/**
 * PlayerInfo-Model
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PlayerInfo {
    private Integer playerId;
    private Integer x;
    private Integer y;
    private Integer index;
    private String name;
    private PlayerColor color;
    private PlayerDirection direction;
}
