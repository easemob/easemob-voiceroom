package com.voiceroom.mic.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.voiceroom.mic.model.VoiceRoom;
import com.voiceroom.mic.service.VoiceRoomService;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Component
public class DeleteVoiceRoomSchedule {

    @Resource
    private VoiceRoomService voiceRoomService;

    @Resource
    private PrometheusMeterRegistry registry;

    @Value("${voice.room.ttl:10}")
    private Long ttl;

    @Value("${local.zone.offset:+8}")
    private String zoneOffset;

    @Scheduled(cron = "${voice.room.delete.scan.time}")
    public void run() {
        try {
            LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.of(zoneOffset));
            LocalDateTime createdTimeStamp = localDateTime.minusMinutes(ttl);
            LambdaQueryWrapper<VoiceRoom> queryWrapper =
                    new LambdaQueryWrapper<VoiceRoom>().lt(VoiceRoom::getCreatedAt, createdTimeStamp);
            List<VoiceRoom> voiceRooms = voiceRoomService.getBaseMapper()
                    .selectList(queryWrapper);
            if (voiceRooms == null || voiceRooms.isEmpty()) {
                return;
            }
            for (VoiceRoom voiceRoom : voiceRooms) {
                voiceRoomService.deleteByRoomId(voiceRoom.getRoomId(), voiceRoom.getOwner());
                log.info("delete expired voice room, room={}", voiceRoom);
            }
        } catch (Exception e) {
            log.error("failed to delete expired room, cause=", e);
            registry.counter("delete.expired.voice.room.error").increment();
        }
    }
}
