package constants.exceptions;

import constants.Codes;

public class ReservationNotFound extends CustomException {
  public ReservationNotFound() {
    super(Codes.RESERVATION_NOT_FOUND);
  }
}