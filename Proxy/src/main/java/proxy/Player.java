package proxy;

import lombok.*;
import proxy.enums.PlayerColor;
import proxy.enums.PlayerDirection;

/**
 * Player-Model
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Player {
    private String name;
    private PlayerColor color;
    private PlayerDirection direction;
}

