drop table if exists user_third_account;
create table user_third_account
(
    `id`         int(10) unsigned not null auto_increment comment 'id',
    `uid`        varchar(100)     not null comment '用户唯一标识',
    `chat_id`    varchar(128)     not null comment '用户对应在环信的用户id',
    `chat_uuid`  varchar(64)      not null comment '用户对应在的环信用户uuid',
    `rtc_uid`    int(32)                   comment 'rtc uid',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_user_third_account_uid` (`uid`)
) engine = InnoDB default charset = utf8mb4 comment ='用户与三方系统的关联信息';

drop table if exists voice_room;
create table voice_room
(
    `id`                    int(10) unsigned not null auto_increment comment 'id',
    `room_id`               varchar(100)     not null comment '房间唯一标识',
    `chatroom_id`           bigint           not null comment '环信关联房间id',
    `channel_id`            varchar(128)     not null comment '声网RTC关联的channelId',
    `name`                  varchar(100)     NOT NULL DEFAULT '' COMMENT '房间名字',
    `is_private`            bool             NOT NULL default true COMMENT '是否公开 0 公开 1加密',
    `password`              varchar(100)     NULL DEFAULT '' COMMENT '密码',
    `allowed_free_join_mic` bool             not null default true comment '是否允许自由上麦',
    `type`                  int(2)           not null comment '房间类型',
    `owner`                 varchar(100)     not null comment '房主uid',
    `sound_effect`          varchar(256)     null comment '房间音效',
    `announcement`          varchar(256) comment '房间公告',
    `use_robot`             bool             not null default false comment '房间是否使用机器人',
    `mic_count`             int(4)           not null default 6 comment '房间麦位数量',
    `robot_count`           int(4)           not null default 2 comment '房间机器人数量',
    `robot_volume`          int(10)      default 50                   not null comment '房间机器人音量',
    `created_at`            timestamp(3)              default CURRENT_timestamp(3) not null comment '创建时间',
    `updated_at`            timestamp(3)              default CURRENT_timestamp(3) not null on update CURRENT_timestamp(3) comment '最近修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_voice_room_room_id` (`room_id`),
    KEY `idx_voice_room_created_at` (`created_at`)
) engine = InnoDB
  default charset = utf8mb4 comment ='语聊房信息';

drop table if exists mic_apply_user;
create table mic_apply_user
(
    `id`         int(10) unsigned not null auto_increment comment 'id',
    `room_id`    varchar(100)     not null comment '房间唯一标识',
    `uid`        varchar(100)     not null comment '用户唯一标识',
    `mic_index`  int(4) comment '申请麦位位置',
    `created_at` timestamp(3)              default CURRENT_timestamp(3) not null comment '创建时间',
    `updated_at` timestamp(3)              default CURRENT_timestamp(3) not null on update CURRENT_timestamp(3) comment '最近修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_voice_room_room_id_uid` (`room_id`, `uid`),
    KEY `idx_voice_room_created_at` (`created_at`)
) engine=InnoDB default charset=utf8mb4 comment='麦位申请信息';

drop table if exists gift_record;
create table gift_record
(
    `id`         int(10) unsigned not null auto_increment comment 'id',
    `room_id`    varchar(100)     not null comment '房间唯一标识',
    `uid`        varchar(100)     not null comment '送礼物用户唯一标识',
    `to_uid`     varchar(100)     not null comment '收礼物用户唯一标识',
    `amount`     bigint(10)       not null default 0 comment '贡献值',
    `created_at` timestamp(3)              default CURRENT_timestamp(3) not null comment '创建时间',
    `updated_at` timestamp(3)              default CURRENT_timestamp(3) not null on update CURRENT_timestamp(3) comment '最近修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_voice_room_room_id_uid_to_uid` (`room_id`, `uid`, `to_uid`),
    KEY `idx_voice_room_amount` (`amount`)
) engine = InnoDB default charset = utf8mb4 comment ='打赏信息';

drop table if exists `user`;
create table `user`
(
    `id`        int(10) unsigned not null auto_increment comment 'id',
    `uid`       varchar(100)     not null comment '用户唯一标识',
    `name`      varchar(100)     not null comment '昵称',
    `portrait`  varchar(100)     not null comment '头像',
    `device_id` varchar(100)     not null comment '设备id',
    `phone`     varchar(20) comment '手机号',
    `created_at`            timestamp(3)              default CURRENT_timestamp(3) not null comment '创建时间',
    `updated_at`            timestamp(3)              default CURRENT_timestamp(3) not null on update CURRENT_timestamp(3) comment '最近修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_user_uid` (`uid`),
    KEY `idx_user_device_id` (`device_id`),
    KEY `idx_user_phone` (`phone`)
) engine = InnoDB default charset = utf8mb4 comment ='用户信息';

drop table if exists `voice_room_user`;
create table `voice_room_user`
(
    `id`        int(10) unsigned not null auto_increment comment 'id',
    `room_id`      varchar(100)     not null comment '房间唯一标识',
    `uid`       varchar(100)     not null comment '用户唯一标识',
    `mic_index` int(4) not null default -1 comment '房间用户麦位信息 -1:没有在麦上，其余标识所在的麦位位置',
    `created_at`            timestamp(3)              default CURRENT_timestamp(3) not null comment '创建时间',
    `updated_at`            timestamp(3)              default CURRENT_timestamp(3) not null on update CURRENT_timestamp(3) comment '最近修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_voice_room_user_room_id_uid` (`room_id`, `uid`)
) engine = InnoDB default charset = utf8mb4 comment ='房间用户信息';
