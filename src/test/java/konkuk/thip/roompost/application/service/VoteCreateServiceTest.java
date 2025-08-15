package konkuk.thip.roompost.application.service;

import konkuk.thip.book.adapter.out.jpa.BookJpaEntity;
import konkuk.thip.book.adapter.out.persistence.repository.BookJpaRepository;
import konkuk.thip.common.util.TestEntityFactory;
import konkuk.thip.room.adapter.out.jpa.CategoryJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantJpaEntity;
import konkuk.thip.room.adapter.out.jpa.RoomParticipantRole;
import konkuk.thip.room.adapter.out.persistence.repository.RoomJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.category.CategoryJpaRepository;
import konkuk.thip.room.adapter.out.persistence.repository.roomparticipant.RoomParticipantJpaRepository;
import konkuk.thip.roompost.application.service.VoteCreateService;
import konkuk.thip.user.adapter.out.jpa.AliasJpaEntity;
import konkuk.thip.user.adapter.out.jpa.UserJpaEntity;
import konkuk.thip.user.adapter.out.persistence.repository.UserJpaRepository;
import konkuk.thip.user.adapter.out.persistence.repository.alias.AliasJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteItemJpaRepository;
import konkuk.thip.roompost.adapter.out.persistence.repository.vote.VoteJpaRepository;
import konkuk.thip.roompost.application.port.in.dto.vote.VoteCreateCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("[단위] 투표 생성 service 단위 테스트")
class VoteCreateServiceTest {

    @Autowired
    private AliasJpaRepository aliasJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private RoomJpaRepository roomJpaRepository;

    @Autowired
    private RoomParticipantJpaRepository roomParticipantJpaRepository;

    @Autowired
    private VoteJpaRepository voteJpaRepository;

    @Autowired
    private VoteItemJpaRepository voteItemJpaRepository;

    @Autowired
    private VoteCreateService voteCreateService;

    @AfterEach
    void tearDown() {
        voteItemJpaRepository.deleteAllInBatch();
        voteJpaRepository.deleteAllInBatch();
        roomParticipantJpaRepository.deleteAllInBatch();
        roomJpaRepository.deleteAllInBatch();
        bookJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
        categoryJpaRepository.deleteAllInBatch();
        aliasJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("유저가 투표를 생성하면, 해당 유저의 [RoomParticipant의 currentPage, userPercentage]와 해당 방의 [Room의 roomPercentage] 값이 변경된다.")
    void vote_create_room_participant_and_room_percentage_update() throws Exception {
        //given
        AliasJpaEntity alias = aliasJpaRepository.save(TestEntityFactory.createScienceAlias());
        UserJpaEntity me = userJpaRepository.save(TestEntityFactory.createUser(alias, "me"));

        BookJpaEntity book = bookJpaRepository.save(TestEntityFactory.createBook(369));
        CategoryJpaEntity category = categoryJpaRepository.save(TestEntityFactory.createScienceCategory(alias));
        RoomJpaEntity room = roomJpaRepository.save(TestEntityFactory.createRoom(book, category));

        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, me, RoomParticipantRole.MEMBER, 0.0));

        UserJpaEntity user = userJpaRepository.save(TestEntityFactory.createUser(alias, "user"));
        roomParticipantJpaRepository.save(TestEntityFactory.createRoomParticipant(room, user, RoomParticipantRole.MEMBER, 7.7));      // userPercentage = 7.7 인 RoomParticipant 생성

        //when
        List<VoteCreateCommand.VoteItemCreateCommand> voteItems = List.of(
                new VoteCreateCommand.VoteItemCreateCommand("투표 항목1"),
                new VoteCreateCommand.VoteItemCreateCommand("투표 항목2")
        );

        VoteCreateCommand command = new VoteCreateCommand(
                me.getUserId(), room.getRoomId(), 89, false, "투표 내용", voteItems
        );

        //then
        voteCreateService.createVote(command);

        //then
        RoomParticipantJpaEntity roomParticipant = roomParticipantJpaRepository.findByUserIdAndRoomId(me.getUserId(), room.getRoomId()).get();
        RoomJpaEntity refreshRoom = roomJpaRepository.findAll().get(0);

        // userPercentage, roomPercentage 값 update 확인
        // 허용 오차범위를 10의 -6제곱(= 0.000001) 로 설정
        assertThat(roomParticipant.getUserPercentage()).isCloseTo((double) 89 / 369 * 100, within(1e-6));
        assertThat(refreshRoom.getRoomPercentage()).isCloseTo((7.7 + (double) 89 / 369 * 100) / 2, within(1e-6));
    }
}
