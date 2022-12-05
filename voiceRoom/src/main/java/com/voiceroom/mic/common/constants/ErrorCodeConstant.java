package com.voiceroom.mic.common.constants;

public interface ErrorCodeConstant {

    String userNotFound = "100404";

    String roomNotFound = "200404";

    String roomUnSupportedOperation = "200401";

    String userNotInRoomError = "201403";

    String voiceRoomTypeMismatch = "200400";

    String createChatroomFailed = "200500";

    String giftNotFound = "300404";

    String micRepeatApplyError = "400400";

    String micApplyError = "400500";

    String micInitError = "401500";

    String micIndexNullError = "402400";

    String micIndexExceedLimitError = "403400";

    String micApplyRecordNotFoundError = "400404";

    String micNotBelongYouError = "400401";

    String micStatusCannotBeModified = "400403";

    String micNotCurrentUser = "401401";

    String micInitAlreadyExists = "401403";

}
