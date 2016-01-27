package cn.coolspan.open.fixbug;

/**
 * Coolspan on 2016/1/26 11:57
 *
 * @author 乔晓松 coolspan@sina.cn
 */
public class FixBugException extends Exception {

    public String message;

    public Throwable throwable;

    public FixBugException(String message) {
        super(message);
        this.message = message;
    }

    public FixBugException(Throwable throwable) {
        super(throwable);
        this.throwable = throwable;
    }

    public FixBugException(String message, Throwable throwable) {
        super(message, throwable);
        this.message = message;
        this.throwable = throwable;
    }
}
