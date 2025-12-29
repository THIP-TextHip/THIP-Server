-- (user_id, room_id) 조합 유니크 제약 추가
ALTER TABLE room_participants
    ADD CONSTRAINT uk_room_participant_user_room
        UNIQUE (user_id, room_id);