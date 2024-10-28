package Vehicles;

import org.dyn4j.geometry.Vector2;

import java.util.HashMap;

public class BlackBoard {
  HashMap<Integer, Vector2> board;

  BlackBoard() {
    board = new HashMap<Integer,Vector2>();
  }

  public void setMessage(int id, Vector2 msg){
    board.put(id,msg);
  }

  public Vector2 getMessage(int id) {
    return board.get(id);
  }
}
