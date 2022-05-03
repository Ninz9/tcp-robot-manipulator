

public class Position {
    public Integer x;
    public Integer y;

    Position(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    Position Turn(String direction) {
        if (direction.equals("LEFT")){
            return new Position(-this.y, this.x);
        } else {
            return new Position( this.y,-this.x);
        }
    }

    Position Clone(){
        return new Position(this.x, this.y);
    }
}
