package constants.exeption;

import constants.Codes;

public class RoomAlreadyFull extends CustomException {
  public RoomAlreadyFull() {
    super(Codes.ROOM_ALREADY_FULL);
  }
}
