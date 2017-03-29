package cz.uhk.machacekgoogle.Exception;

/**
 * @author Jan Macháček
 *         Created on 15.8.2016.
 */
public class InvalidateRefreshTokenException extends Exception {
    public InvalidateRefreshTokenException(String message) {
        super(message);
    }
}
