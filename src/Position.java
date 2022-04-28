

public class Position {
    public Integer x;
    public Integer y;

    Position(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    Position Turn(String direction) {
        if (direction.equals("LEFT")){
            return new Position(-1 * this.y, this.x);
        } else {
            return new Position( this.y,-1 * this.x);
        }
    }
}
