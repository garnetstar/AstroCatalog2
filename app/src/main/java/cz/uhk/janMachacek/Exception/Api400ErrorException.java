package cz.uhk.janMachacek.Exception;

/**
 * @author Jan Macháček
 *         Created on 29.1.2017.
 */
public class Api400ErrorException extends Exception
{
    public Api400ErrorException(String message) {
        super(message);
    }
}
