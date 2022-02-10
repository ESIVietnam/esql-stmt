package esql.common;

public class ESQLStmtException extends Exception  {
    protected final String message;
    protected final Integer code;
    protected final String name;

    public ESQLStmtException(String message) {
        super(message);
        this.name = null;
        this.code = null;
        this.message = message;
    }

    //cascade error
    public ESQLStmtException(String name, ESQLStmtException e) {
        super(e.getCause());
        if(e.getName()==null)
            this.name = name;
        else
            this.name = name+"/"+e.getName();
        this.code = e.code;
        this.message = e.message;
    }
    //cascade error
    public ESQLStmtException(int code, ESQLStmtException e) {
        super(e);
        if(e.code==null)
            this.code = code;
        else
            this.code = e.code;
        this.name = e.name;
        this.message = e.message;
    }
    //cascade error
    public ESQLStmtException(String name, int code, ESQLStmtException e) {
        super(e);
        if(e.name==null)
            this.name = name;
        else this.name = name+"/"+e.getName();
        if(e.code==null)
            this.code = code;
        else
            this.code = e.code;
        this.message = e.message;
    }

    public ESQLStmtException(Throwable cause) {
        super(cause);
        this.name = null;
        this.code = null;
        this.message = "[" +cause.getClass().getName()+"]: "+cause.getMessage();
    }

    public ESQLStmtException(String message, Throwable cause) {
        super(cause);
        this.name = null;
        this.code = null;
        this.message = message + ". [" +cause.getClass().getName()+"]: "+ cause.getMessage();
    }

    public ESQLStmtException(String name, String message, Throwable cause) {
        super(cause);
        this.name = name;
        this.code = null;
        this.message = message + ". [" +cause.getClass().getName()+"]: "+ cause.getMessage();
    }

    public ESQLStmtException(int code, String message) {
        super(message);
        this.message = message;
        this.code = code;
        this.name = null;
    }

    public ESQLStmtException(String name, String message) {
        super(message);
        this.message = message;
        this.name = name;
        this.code = null;
    }

    public ESQLStmtException(String name, int code, String message) {
        super(message);
        this.message = message;
        this.name = name;
        this.code = code;
    }

    @Override
    public String toString() {
        StringBuilder sb =  new StringBuilder("{");
        if(code != null)
            sb.append("code=").append(code).append(", ");
        if(name != null)
            sb.append("name=").append(name).append(", ");
        if(message != null)
            sb.append("message=").append(message);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String getMessage() {
        return message;
    }

    public int getCode() {
        if(code != null)
            return this.code;
        return 0;//meaning no error
    }

    public String getName() {
        return this.name;
    }
}
