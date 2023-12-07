package proxy;

import lombok.*;

/**
 * LoginAction-Model
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginAction {
    private String playerName;
    private int playerId;
}
