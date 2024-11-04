package proyecto4;

public class User{
    public String name;
    public String password;

    public User(String name, String password){
        this.name = name;
        this.password = password;
    }

    @Override
    public String toString(){
        return this.name +" "+ this.password;
    }
}
