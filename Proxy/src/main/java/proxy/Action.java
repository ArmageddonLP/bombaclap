package proxy;

import lombok.*;
import proxy.enums.PlayerDirection;

/**
 * PlayerAction-Model
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Action {
    private Integer playerId;
    private PlayerDirection playerDirection;
    private Boolean bombPlanted;
}
