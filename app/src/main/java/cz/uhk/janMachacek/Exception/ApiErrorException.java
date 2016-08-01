package cz.uhk.janMachacek.Exception;

/**
 * Created by jan on 26.7.2016.
 */
public class ApiErrorException extends Exception {

    public ApiErrorException() {

    }

    public ApiErrorException(String message) {
        super(message);
    }

    public ApiErrorException(String message, Throwable e) {
        super(message, e);
    }
}
