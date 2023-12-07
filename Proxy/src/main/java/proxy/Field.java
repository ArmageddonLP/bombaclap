package proxy;

import lombok.*;
import proxy.enums.BlockType;
import proxy.enums.BombState;

/**
 * Field-Model
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Field {
    private Player[] players = new Player[4];
    private int x, y;
    private BlockType ground;
    private BombState bombState;
    private long bombTimer;
}
