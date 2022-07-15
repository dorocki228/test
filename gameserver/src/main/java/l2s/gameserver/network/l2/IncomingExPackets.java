package l2s.gameserver.network.l2;

import l2s.commons.network.IConnectionState;
import l2s.commons.network.IIncomingPacket;
import l2s.commons.network.IIncomingPackets;
import l2s.gameserver.network.l2.c2s.*;
import l2s.gameserver.network.l2.c2s.bookmark.ExBookmarkPacket;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Sdw
 */
public enum IncomingExPackets implements IIncomingPackets<GameClient> {
    EX_DUMMY(null, ConnectionState.IN_GAME),
    EX_REQ_MANOR_LIST(RequestManorList::new, ConnectionState.JOINING_GAME),
    // TODO manor REQUEST_PROCEDURE_CROP_LIST(0x02, RequestProcureCropList::new, ConnectionState.IN_GAME),
    EX_PROCURE_CROP_LIST(null, ConnectionState.IN_GAME),
    // TODO manor REQUEST_SET_SEED(0x03, RequestSetSeed::new, ConnectionState.IN_GAME),
    EX_SET_SEED(null, ConnectionState.IN_GAME),
    // TODO manor REQUEST_SET_CROP(0x04, RequestSetCrop::new, ConnectionState.IN_GAME),
    EX_SET_CROP(null, ConnectionState.IN_GAME),
    EX_WRITE_HERO_WORDS(RequestWriteHeroWords::new, ConnectionState.IN_GAME),
    EX_ASK_JOIN_MPCC(RequestExAskJoinMPCC::new, ConnectionState.IN_GAME),
    EX_ACCEPT_JOIN_MPCC(RequestExAcceptJoinMPCC::new, ConnectionState.IN_GAME),
    EX_OUST_FROM_MPCC(RequestExOustFromMPCC::new, ConnectionState.IN_GAME),
    EX_OUST_FROM_PARTY_ROOM(RequestOustFromPartyRoom::new, ConnectionState.IN_GAME),
    EX_DISMISS_PARTY_ROOM(RequestDismissPartyRoom::new, ConnectionState.IN_GAME),
    EX_WITHDRAW_PARTY_ROOM(RequestWithdrawPartyRoom::new, ConnectionState.IN_GAME),
    EX_HAND_OVER_PARTY_MASTER(RequestChangePartyLeader::new, ConnectionState.IN_GAME),
    EX_AUTO_SOULSHOT(RequestAutoSoulShot::new, ConnectionState.IN_GAME),
    // TODO REQUEST_EX_ENCHANT_SKILL_INFO(0x0E, RequestExEnchantSkillInfo::new, ConnectionState.IN_GAME),
    EX_ENCHANT_SKILL_INFO(null, ConnectionState.IN_GAME),
    // TODO REQUEST_EX_ENCHANT_SKILL(0x0F, RequestExEnchantSkill::new, ConnectionState.IN_GAME),
    EX_REQ_ENCHANT_SKILL(null, ConnectionState.IN_GAME),
    EX_PLEDGE_EMBLEM(RequestExPledgeCrestLarge::new, ConnectionState.IN_GAME),
    EX_SET_PLEDGE_EMBLEM(RequestExSetPledgeCrestLarge::new, ConnectionState.IN_GAME),
    EX_SET_ACADEMY_MASTER(RequestPledgeSetAcademyMaster::new, ConnectionState.IN_GAME),
    EX_PLEDGE_POWER_GRADE_LIST(RequestPledgePowerGradeList::new, ConnectionState.IN_GAME),
    EX_VIEW_PLEDGE_POWER(RequestPledgeMemberPowerInfo::new, ConnectionState.IN_GAME),
    EX_SET_PLEDGE_POWER_GRADE(RequestPledgeSetMemberPowerGrade::new, ConnectionState.IN_GAME),
    EX_VIEW_PLEDGE_MEMBER_INFO(RequestPledgeMemberInfo::new, ConnectionState.IN_GAME),
    EX_VIEW_PLEDGE_WARLIST(RequestPledgeWarList::new, ConnectionState.IN_GAME),
    EX_FISH_RANKING(RequestExFishRanking::new, ConnectionState.IN_GAME),
    EX_PCCAFE_COUPON_USE(RequestPCCafeCouponUse::new, ConnectionState.IN_GAME),
    EX_ORC_MOVE(null, ConnectionState.IN_GAME),
    EX_DUEL_ASK_START(RequestDuelStart::new, ConnectionState.IN_GAME),
    EX_DUEL_ACCEPT_START(RequestDuelAnswerStart::new, ConnectionState.IN_GAME),
    EX_SET_TUTORIAL(null, ConnectionState.IN_GAME),
    EX_RQ_ITEMLINK(RequestExRqItemLink::new, ConnectionState.IN_GAME),
    EX_CAN_NOT_MOVE_ANYMORE_IN_AIRSHIP(null, ConnectionState.IN_GAME),
    EX_MOVE_TO_LOCATION_IN_AIRSHIP(MoveToLocationInAirShip::new, ConnectionState.IN_GAME),
    EX_LOAD_UI_SETTING(RequestKeyMapping::new, ConnectionState.JOINING_GAME),
    EX_SAVE_UI_SETTING(RequestSaveKeyMapping::new, ConnectionState.IN_GAME),
    EX_REQUEST_BASE_ATTRIBUTE_CANCEL(RequestExRemoveItemAttribute::new, ConnectionState.IN_GAME),
    EX_CHANGE_INVENTORY_SLOT(RequestSaveInventoryOrder::new, ConnectionState.IN_GAME),
    EX_EXIT_PARTY_MATCHING_WAITING_ROOM(RequestExitPartyMatchingWaitingRoom::new, ConnectionState.IN_GAME),
    EX_TRY_TO_PUT_ITEM_FOR_VARIATION_MAKE(RequestConfirmTargetItem::new, ConnectionState.IN_GAME),
    EX_TRY_TO_PUT_INTENSIVE_FOR_VARIATION_MAKE(RequestConfirmRefinerItem::new, ConnectionState.IN_GAME),
    EX_TRY_TO_PUT_COMMISSION_FOR_VARIATION_MAKE(RequestConfirmGemStone::new, ConnectionState.IN_GAME),
    EX_OLYMPIAD_OBSERVER_END(RequestOlympiadObserverEnd::new, ConnectionState.IN_GAME),
    // TODO REQUEST_CURSED_WEAPON_LIST(0x2A, RequestCursedWeaponList::new, ConnectionState.IN_GAME),
    EX_CURSED_WEAPON_LIST(null, ConnectionState.IN_GAME),
    // TODO REQUEST_CURSED_WEAPON_LOCATION(0x2B, RequestCursedWeaponLocation::new, ConnectionState.IN_GAME),
    EX_EXISTING_CURSED_WEAPON_LOCATION(null, ConnectionState.IN_GAME),
    EX_REORGANIZE_PLEDGE_MEMBER(RequestPledgeReorganizeMember::new, ConnectionState.IN_GAME),
    EX_MPCC_SHOW_PARTY_MEMBERS_INFO(RequestExMPCCShowPartyMembersInfo::new, ConnectionState.IN_GAME),
    EX_OLYMPIAD_MATCH_LIST(RequestExOlympiadMatchListRefresh::new, ConnectionState.IN_GAME),
    EX_ASK_JOIN_PARTY_ROOM(RequestAskJoinPartyRoom::new, ConnectionState.IN_GAME),
    EX_ANSWER_JOIN_PARTY_ROOM(AnswerJoinPartyRoom::new, ConnectionState.IN_GAME),
    EX_LIST_PARTY_MATCHING_WAITING_ROOM(RequestListPartyMatchingWaitingRoom::new, ConnectionState.IN_GAME),
    // TODO REQUEST_EX_ENCHANT_ITEM_ATTRIBUTE(0x32, RequestExEnchantItemAttribute::new, ConnectionState.IN_GAME),
    EX_CHOOSE_INVENTORY_ATTRIBUTE_ITEM(null, ConnectionState.IN_GAME),
    EX_CHARACTER_BACK(RequestGotoLobby::new, ConnectionState.AUTHENTICATED),
    // TODO implement packet
    EX_CANNOT_AIRSHIP_MOVE_ANYMORE(null, ConnectionState.IN_GAME),
    // TODO MOVE_TO_LOCATION_AIR_SHIP(0x35, MoveToLocationAirShip::new, ConnectionState.IN_GAME),
    EX_MOVE_TO_LOCATION_AIRSHIP(null, ConnectionState.IN_GAME),
    // TODO REQUEST_BID_ITEM_AUCTION(0x36, RequestBidItemAuction::new, ConnectionState.IN_GAME),
    EX_ITEM_AUCTION_BID(null, ConnectionState.IN_GAME),
    // TODO REQUEST_INFO_ITEM_AUCTION(0x37, RequestInfoItemAuction::new, ConnectionState.IN_GAME),
    EX_ITEM_AUCTION_INFO(null, ConnectionState.IN_GAME),
    EX_CHANGE_NAME(RequestExChangeName::new, ConnectionState.IN_GAME),
    EX_SHOW_CASTLE_INFO(RequestAllCastleInfo::new, ConnectionState.IN_GAME),
    EX_SHOW_FORTRESS_INFO(RequestAllFortressInfo::new, ConnectionState.IN_GAME),
    EX_SHOW_AGIT_INFO(RequestAllAgitInfo::new, ConnectionState.IN_GAME),
    // TODO REQUEST_FORTRESS_SIEGE_INFO(0x3C, RequestFortressSiegeInfo::new, ConnectionState.IN_GAME),
    EX_SHOW_FORTRESS_SIEGE_INFO(null, ConnectionState.IN_GAME),
    // TODO REQUEST_GET_BOSS_RECORD(0x3D, RequestGetBossRecord::new, ConnectionState.IN_GAME),
    EX_GET_BOSS_RECORD(null, ConnectionState.IN_GAME),
    EX_TRY_TO_MAKE_VARIATION(RequestRefine::new, ConnectionState.IN_GAME),
    EX_TRY_TO_PUT_ITEM_FOR_VARIATION_CANCEL(RequestConfirmCancelItem::new, ConnectionState.IN_GAME),
    EX_CLICK_VARIATION_CANCEL_BUTTON(RequestRefineCancel::new, ConnectionState.IN_GAME),
    EX_MAGIC_SKILL_USE_GROUND(RequestExMagicSkillUseGround::new, ConnectionState.IN_GAME),
    EX_DUEL_SURRENDER(RequestDuelSurrender::new, ConnectionState.IN_GAME),
    // TODO REQUEST_EX_ENCHANT_SKILL_INFO_DETAIL(0x43, RequestExEnchantSkillInfoDetail::new, ConnectionState.IN_GAME),
    EX_ENCHANT_SKILL_INFO_DETAIL(null, ConnectionState.IN_GAME),
    // TODO implement packet
    EX_REQUEST_ANTI_FREE_SERVER(null, ConnectionState.IN_GAME),
    // TODO REQUEST_FORTRESS_MAP_INFO(0x45, RequestFortressMapInfo::new, ConnectionState.IN_GAME),
    EX_SHOW_FORTRESS_MAP_INFO(null, ConnectionState.IN_GAME),
    EX_REQUEST_PVPMATCH_RECORD(RequestPVPMatchRecord::new, ConnectionState.IN_GAME),
    EX_PRIVATE_STORE_WHOLE_SET_MSG(SetPrivateStoreWholeMsg::new, ConnectionState.IN_GAME),
    EX_DISPEL(RequestDispel::new, ConnectionState.IN_GAME),
    EX_TRY_TO_PUT_ENCHANT_TARGET_ITEM(RequestExTryToPutEnchantTargetItem::new, ConnectionState.IN_GAME),
    EX_TRY_TO_PUT_ENCHANT_SUPPORT_ITEM(RequestExTryToPutEnchantSupportItem::new, ConnectionState.IN_GAME),
    EX_CANCEL_ENCHANT_ITEM(RequestExCancelEnchantItem::new, ConnectionState.IN_GAME),
    EX_CHANGE_NICKNAME_COLOR(RequestChangeNicknameColor::new, ConnectionState.IN_GAME),
    EX_REQUEST_RESET_NICKNAME(RequestResetNickname::new, ConnectionState.IN_GAME),
    EX_USER_BOOKMARK(ExBookmarkPacket::new, ConnectionState.IN_GAME),
    // TODO REQUEST_WITHDRAW_PREMIUM_ITEM(0x4F, RequestWithDrawPremiumItem::new, ConnectionState.IN_GAME),
    EX_WITHDRAW_PREMIUM_ITEM(null, ConnectionState.IN_GAME),
    EX_JUMP(null, ConnectionState.IN_GAME),
    // TODO REQUEST_EX_START_SHOW_CRATAE_CUBE_RANK(0x51, RequestStartShowKrateisCubeRank::new, ConnectionState.IN_GAME),
    EX_START_REQUEST_PVPMATCH_CC_RANK(null, ConnectionState.IN_GAME),
    // TODO REQUEST_EX_STOP_SHOW_CRATAE_CUBE_RANK(0x52, RequestStopShowKrateisCubeRank::new, ConnectionState.IN_GAME),
    EX_STOP_REQUEST_PVPMATCH_CC_RANK(null, ConnectionState.IN_GAME),
    EX_NOTIFY_START_MINIGAME(null, ConnectionState.IN_GAME),
    EX_REQUEST_REGISTER_DOMINION(null, ConnectionState.IN_GAME),
    EX_REQUEST_DOMINION_INFO(null, ConnectionState.IN_GAME),
    // TODO REQUEST_EX_CLEFT_ENTER(0x56, RequestExCleftEnter::new, ConnectionState.IN_GAME),
    EX_CLEFT_ENTER(null, ConnectionState.IN_GAME),
    // TODO REQUEST_EX_CUBE_GAME_CHANGE_TEAM(0x57, RequestExCubeGameChangeTeam::new, ConnectionState.IN_GAME),
    EX_BLOCK_UPSET_ENTER(null, ConnectionState.IN_GAME),
    EX_END_SCENE_PLAYER(EndScenePlayer::new, ConnectionState.IN_GAME),
    // TODO EX_BLOCK_UPSET_VOTE(0x59, RequestExCubeGameReadyAnswer::new, ConnectionState.IN_GAME),
    EX_BLOCK_UPSET_VOTE(null, ConnectionState.IN_GAME),
    EX_LIST_MPCC_WAITING(RequestExListMpccWaiting::new, ConnectionState.IN_GAME),
    EX_MANAGE_MPCC_ROOM(RequestExManageMpccRoom::new, ConnectionState.IN_GAME),
    EX_JOIN_MPCC_ROOM(RequestExJoinMpccRoom::new, ConnectionState.IN_GAME),
    EX_OUST_FROM_MPCC_ROOM(RequestExOustFromMpccRoom::new, ConnectionState.IN_GAME),
    EX_DISMISS_MPCC_ROOM(RequestExDismissMpccRoom::new, ConnectionState.IN_GAME),
    EX_WITHDRAW_MPCC_ROOM(RequestExWithdrawMpccRoom::new, ConnectionState.IN_GAME),
    EX_SEED_PHASE(RequestSeedPhase::new, ConnectionState.IN_GAME),
    EX_MPCC_PARTYMASTER_LIST(RequestExMpccPartymasterList::new, ConnectionState.IN_GAME),
    EX_REQUEST_POST_ITEM_LIST(RequestPostItemList::new, ConnectionState.IN_GAME),
    EX_SEND_POST(RequestSendPost::new, ConnectionState.IN_GAME),
    EX_REQUEST_RECEIVED_POST_LIST(RequestReceivedPostList::new, ConnectionState.IN_GAME),
    EX_DELETE_RECEIVED_POST(RequestDeleteReceivedPost::new, ConnectionState.IN_GAME),
    EX_REQUEST_RECEIVED_POST(RequestReceivedPost::new, ConnectionState.IN_GAME),
    EX_RECEIVE_POST(RequestPostAttachment::new, ConnectionState.IN_GAME),
    EX_REJECT_POST(RequestRejectPostAttachment::new, ConnectionState.IN_GAME),
    EX_REQUEST_SENT_POST_LIST(RequestSentPostList::new, ConnectionState.IN_GAME),
    EX_DELETE_SENT_POST(RequestDeleteSentPost::new, ConnectionState.IN_GAME),
    EX_REQUEST_SENT_POST(RequestSentPost::new, ConnectionState.IN_GAME),
    EX_CANCEL_SEND_POST(RequestCancelPostAttachment::new, ConnectionState.IN_GAME),
    EX_REQUEST_SHOW_PETITION(RequestShowNewUserPetition::new, ConnectionState.IN_GAME),
    EX_REQUEST_SHOWSTEP_TWO(RequestShowStepTwo::new, ConnectionState.IN_GAME),
    EX_REQUEST_SHOWSTEP_THREE(RequestShowStepThree::new, ConnectionState.IN_GAME),
    // TODO EX_CONNECT_TO_RAID_SERVER(0x70, null, ConnectionState.IN_GAME),
    EX_CONNECT_TO_RAID_SERVER(null, ConnectionState.IN_GAME),
    // TODO EX_RETURN_FROM_RAID_SERVER(0x71, null, ConnectionState.IN_GAME),
    EX_RETURN_FROM_RAID(null, ConnectionState.IN_GAME),
    EX_REFUND_REQ(RequestRefundItem::new, ConnectionState.IN_GAME),
    EX_BUY_SELL_UI_CLOSE_REQ(RequestBuySellUIClose::new, ConnectionState.IN_GAME),
    // TODO REQUEST_EX_EVENT_MATCH_OBSERVER_END(0x74, null, ConnectionState.IN_GAME),
    EX_EVENT_MATCH(null, ConnectionState.IN_GAME),
    EX_PARTY_LOOTING_MODIFY(RequestPartyLootModification::new, ConnectionState.IN_GAME),
    EX_PARTY_LOOTING_MODIFY_AGREEMENT(AnswerPartyLootModification::new, ConnectionState.IN_GAME),
    EX_ANSWER_COUPLE_ACTION(AnswerCoupleAction::new, ConnectionState.IN_GAME),
    EX_BR_LOAD_EVENT_TOP_RANKERS_REQ(BrEventRankerList::new, ConnectionState.IN_GAME),
    EX_ASK_MY_MEMBERSHIP(null, ConnectionState.IN_GAME),
    EX_QUEST_NPC_LOG_LIST(RequestAddExpandQuestAlarm::new, ConnectionState.IN_GAME),
    EX_VOTE_SYSTEM(RequestVoteNew::new, ConnectionState.IN_GAME),
    EX_GETON_SHUTTLE(RequestShuttleGetOn::new, ConnectionState.IN_GAME),
    EX_GETOFF_SHUTTLE(RequestShuttleGetOff::new, ConnectionState.IN_GAME),
    EX_MOVE_TO_LOCATION_IN_SHUTTLE(MoveToLocationInShuttle::new, ConnectionState.IN_GAME),
    EX_CAN_NOT_MOVE_ANYMORE_IN_SHUTTLE(CannotMoveAnymoreInShuttle::new, ConnectionState.IN_GAME),
    EX_AGITAUCTION_CMD(null, ConnectionState.IN_GAME), // TODO: Implement / HANDLE SWITCH
    EX_ADD_POST_FRIEND(RequestExAddContactToContactList::new, ConnectionState.IN_GAME),
    EX_DELETE_POST_FRIEND(RequestExDeleteContactFromContactList::new, ConnectionState.IN_GAME),
    EX_SHOW_POST_FRIEND(RequestExShowContactList::new, ConnectionState.IN_GAME),
    EX_FRIEND_LIST_FOR_POSTBOX(RequestExFriendListExtended::new, ConnectionState.IN_GAME),
    EX_GFX_OLYMPIAD(RequestExOlympiadMatchListRefresh::new, ConnectionState.IN_GAME),
    EX_BR_GAME_POINT_REQ(RequestBRGamePoint::new, ConnectionState.IN_GAME),
    // TODO REQUEST_BR_PRODUCT_LIST(0x87, RequestBRProductList::new, ConnectionState.IN_GAME),
    EX_BR_PRODUCT_LIST_REQ(null, ConnectionState.IN_GAME),
    // TODO REQUEST_BR_PRODUCT_INFO(0x88, RequestBRProductInfo::new, ConnectionState.IN_GAME),
    EX_BR_PRODUCT_INFO_REQ(null, ConnectionState.IN_GAME),
    EX_BR_BUY_PRODUCT_REQ(RequestBRBuyProduct::new, ConnectionState.IN_GAME),
    // TODO REQUEST_BR_RECENT_PRODUCT_LIST(0x8A, RequestBRRecentProductList::new, ConnectionState.IN_GAME),
    EX_BR_RECENT_PRODUCT_REQ(null, ConnectionState.IN_GAME),
    EX_BR_MINIGAME_LOAD_SCORES_REQ(RequestBRMiniGameLoadScores::new, ConnectionState.IN_GAME),
    EX_BR_MINIGAME_INSERT_SCORE_REQ(RequestBRMiniGameInsertScore::new, ConnectionState.IN_GAME),
    EX_BR_SET_LECTURE_MARK_REQ(RequestExBRLectureMark::new, ConnectionState.IN_GAME),
    EX_REQUEST_CRYSTALITEM_INFO(RequestCrystalItemInfo::new, ConnectionState.IN_GAME),
    EX_REQUEST_CRYSTALITEM_CANCEL(RequestCrystallizeItemCancel::new, ConnectionState.IN_GAME),
    EX_STOP_SCENE_PLAYER(RequestExEscapeScene::new, ConnectionState.IN_GAME),
    EX_FLY_MOVE(null, ConnectionState.IN_GAME), // RequestFlyMove - Sayune is not available on classic yet
    EX_SURRENDER_PLEDGE_WAR(null, ConnectionState.IN_GAME),
    EX_DYNAMIC_QUEST(null, ConnectionState.IN_GAME), // TODO: Implement / HANDLE SWITCH
    EX_FRIEND_DETAIL_INFO(RequestFriendDetailInfo::new, ConnectionState.IN_GAME),
    EX_UPDATE_FRIEND_MEMO(RequestUpdateFriendMemo::new, ConnectionState.IN_GAME),
    EX_UPDATE_BLOCK_MEMO(RequestUpdateBlockMemo::new, ConnectionState.IN_GAME),
    // TODO REQUEST_INZONE_PARTY_INFO_HISTORY(0x97, null, ConnectionState.IN_GAME),
    EX_LOAD_INZONE_PARTY_HISTORY(null, ConnectionState.IN_GAME),
    // TODO REQUEST_COMMISSION_REGISTRABLE_ITEM_LIST(0x98, RequestCommissionRegistrableItemList::new, ConnectionState.IN_GAME),
    EX_REQUEST_COMMISSION_ITEM_LIST(null, ConnectionState.IN_GAME),
    EX_REQUEST_COMMISSION_INFO(RequestCommissionInfo::new, ConnectionState.IN_GAME),
    // TODO REQUEST_COMMISSION_REGISTER(0x9A, RequestCommissionRegister::new, ConnectionState.IN_GAME),
    EX_REQUEST_COMMISSION_REGISTER(null, ConnectionState.IN_GAME),
    EX_REQUEST_COMMISSION_CANCEL(RequestCommissionCancel::new, ConnectionState.IN_GAME),
    // TODO REQUEST_COMMISSION_DELETE(0x9C, RequestCommissionDelete::new, ConnectionState.IN_GAME),
    EX_REQUEST_COMMISSION_DELETE(null, ConnectionState.IN_GAME),
    // TODO REQUEST_COMMISSION_LIST(0x9D, RequestCommissionList::new, ConnectionState.IN_GAME),
    EX_REQUEST_COMMISSION_SEARCH(null, ConnectionState.IN_GAME),
    // TODO REQUEST_COMMISSION_BUY_INFO(0x9E, RequestCommissionBuyInfo::new, ConnectionState.IN_GAME)
    EX_REQUEST_COMMISSION_BUY_INFO(null, ConnectionState.IN_GAME),
    // TODO REQUEST_COMMISSION_BUY_ITEM(0x9F, RequestCommissionBuyItem::new, ConnectionState.IN_GAME),
    EX_REQUEST_COMMISSION_BUY_ITEM(null, ConnectionState.IN_GAME),
    // TODO REQUEST_COMMISSION_REGISTERED_ITEM(0xA0, RequestCommissionRegisteredItem::new, ConnectionState.IN_GAME),
    EX_REQUEST_COMMISSION_REGISTERED_ITEM(null, ConnectionState.IN_GAME),
    // TODO REQUEST_CALL_TO_CHANGE_CLASS(0xA1, null, ConnectionState.IN_GAME),
    EX_CALL_TO_CHANGE_CLASS(null, ConnectionState.IN_GAME),
    // TODO REQUEST_CHANGE_TO_AWAKENED_CLASS(0xA2, RequestChangeToAwakenedClass::new, ConnectionState.IN_GAME),
    EX_CHANGE_TO_AWAKENED_CLASS(null, ConnectionState.IN_GAME),
    // TODO REQUEST_WORLD_STATISTICS(0xA3, null, ConnectionState.IN_GAME),
    EX_NOT_USED_163(null, ConnectionState.IN_GAME),
    // TODO REQUEST_USER_STATISTICS(0xA4, null, ConnectionState.IN_GAME),
    EX_NOT_USED_164(null, ConnectionState.IN_GAME),
    // TODO REQUEST_24HZ_SESSION_ID(0xA5, null, ConnectionState.IN_GAME),
    EX_REQUEST_WEB_SESSION_ID(null, ConnectionState.IN_GAME),
    EX_2ND_PASSWORD_CHECK(RequestEx2ndPasswordCheck::new, ConnectionState.AUTHENTICATED),
    EX_2ND_PASSWORD_VERIFY(RequestEx2ndPasswordVerify::new, ConnectionState.AUTHENTICATED),
    EX_2ND_PASSWORD_REQ(RequestEx2ndPasswordReq::new, ConnectionState.AUTHENTICATED),
    EX_CHECK_CHAR_NAME(RequestCharacterNameCreatable::new, ConnectionState.AUTHENTICATED),
    // TODO REQUEST_GOODS_INVENTORY_INFO(0xAA, null, ConnectionState.IN_GAME),
    EX_REQUEST_GOODS_INVENTORY_INFO(null, ConnectionState.IN_GAME),
    // TODO REQUEST_GOODS_INVENTORY_ITEM(0xAB, null, ConnectionState.IN_GAME),
    EX_REQUEST_USE_GOODS_IVENTORY_ITEM(null, ConnectionState.IN_GAME),
    // TODO REQUEST_FIRST_PLAY_START(0xAC, null, ConnectionState.IN_GAME),
    EX_NOTIFY_PLAY_START(null, ConnectionState.IN_GAME),
    // TODO REQUEST_FLY_MOVE_START(0xAD, null, ConnectionState.IN_GAME), // RequestFlyMoveStart - Sayune is not available on classic yet
    EX_FLY_MOVE_START(null, ConnectionState.IN_GAME), // RequestFlyMoveStart - Sayune is not available on classic yet
    // TODO REQUEST_HARDWARE_INFO(0xAE, RequestHardWareInfo::new, ConnectionState.values()),
    EX_USER_HARDWARE_INFO(RequestHardWareInfo::new, ConnectionState.values()),
    EX_USER_INTERFACE_INFO(null, ConnectionState.IN_GAME),
    EX_CHANGE_ATTRIBUTE_ITEM(SendChangeAttributeTargetItem::new, ConnectionState.IN_GAME),
    EX_REQUEST_CHANGE_ATTRIBUTE(RequestChangeAttributeItem::new, ConnectionState.IN_GAME),
    EX_CHANGE_ATTRIBUTE_CANCEL(RequestChangeAttributeCancel::new, ConnectionState.IN_GAME),
    // TODO REQUEST_BR_PRESENT_BUY_PRODUCT(0xB3, RequestBRPresentBuyProduct::new, ConnectionState.IN_GAME),
    EX_BR_BUY_PRODUCT_GIFT_REQ(null, ConnectionState.IN_GAME),
    // TODO CONFIRM_MENTEE_ADD(0xB4, ConfirmMenteeAdd::new, ConnectionState.IN_GAME),
    EX_MENTOR_ADD(null, ConnectionState.IN_GAME),
    // TODO REQUEST_MENTOR_CANCEL(0xB5, RequestMentorCancel::new, ConnectionState.IN_GAME),
    EX_MENTOR_CANCEL(null, ConnectionState.IN_GAME),
    // TODO REQUEST_MENTOR_LIST(0xB6, RequestMentorList::new, ConnectionState.IN_GAME),
    EX_MENTOR_LIST(null, ConnectionState.IN_GAME),
    // TODO REQUEST_MENTEE_ADD(0xB7, RequestMenteeAdd::new, ConnectionState.IN_GAME),
    EX_REQUEST_MENTOR_ADD(null, ConnectionState.IN_GAME),
    // TODO REQUEST_MENTEE_WAITING_LIST(0xB8, RequestMenteeWaitingList::new, ConnectionState.IN_GAME),
    EX_MENTEE_WAITING_LIST(null, ConnectionState.IN_GAME),
    EX_JOIN_PLEDGE_BY_NAME(RequestClanAskJoinByName::new, ConnectionState.IN_GAME),
    EX_INZONE_WAITING_TIME(RequestInzoneWaitingTime::new, ConnectionState.IN_GAME),
    // TODO REQUEST_JOIN_CURIOUS_HOUSE(0xBB, RequestJoinCuriousHouse::new, ConnectionState.IN_GAME),
    EX_JOIN_CURIOUS_HOUSE(null, ConnectionState.IN_GAME),
    // TODO REQUEST_CANCEL_CURIOUS_HOUSE(0xBC, RequestCancelCuriousHouse::new, ConnectionState.IN_GAME),
    EX_CANCEL_CURIOUS_HOUSE(null, ConnectionState.IN_GAME),
    // TODO REQUEST_LEAVE_CURIOUS_HOUSE(0xBD, null, ConnectionState.IN_GAME),
    EX_LEAVE_CURIOUS_HOUSE(null, ConnectionState.IN_GAME),
    // TODO REQUEST_OBSERVING_LIST_CURIOUS_HOUSE(0xBE, null, ConnectionState.IN_GAME),
    EX_OBSERVE_LIST_CURIOUS_HOUSE(null, ConnectionState.IN_GAME),
    // TODO REQUEST_OBSERVING_CURIOUS_HOUSE(0xBF, null, ConnectionState.IN_GAME),
    EX_OBSERVE_CURIOUS_HOUSE(null, ConnectionState.IN_GAME),
    // TODO REQUEST_LEAVE_OBSERVING_CURIOUS_HOUSE(0xC0, null, ConnectionState.IN_GAME),
    EX_EXIT_OBSERVE_CURIOUS_HOUSE(null, ConnectionState.IN_GAME),
    // TODO REQUEST_CURIOUS_HOUSE_HTML(0xC1, RequestCuriousHouseHtml::new, ConnectionState.IN_GAME),
    EX_REQ_CURIOUS_HOUSE_HTML(null, ConnectionState.IN_GAME),
    // TODO REQUEST_CURIOUS_HOUSE_RECORD(0xC2, null, ConnectionState.IN_GAME),
    EX_REQ_CURIOUS_HOUSE_RECORD(null, ConnectionState.IN_GAME),
    // TODO EX_SYSSTRING(0xC3, null, ConnectionState.IN_GAME),
    EX_SYS_STRING(null, ConnectionState.IN_GAME),
    EX_TRY_TO_PUT_SHAPE_SHIFTING_TARGET_ITEM(RequestExTryToPutShapeShiftingTargetItem::new, ConnectionState.IN_GAME), // RequestExTryToPutShapeShiftingTargetItem - Appearance Stone not used on classic
    EX_TRY_TO_PUT_SHAPE_SHIFTING_EXTRACTION_ITEM(RequestExTryToPutShapeShiftingEnchantSupportItem::new, ConnectionState.IN_GAME), // RequestExTryToPutShapeShiftingEnchantSupportItem - Appearance Stone not used on classic
    EX_CANCEL_SHAPE_SHIFTING(RequestExCancelShapeShiftingItem::new, ConnectionState.IN_GAME), // RequestExCancelShape_Shifting_Item - Appearance Stone not used on classic
    EX_REQUEST_SHAPE_SHIFTING(RequestShapeShiftingItem::new, ConnectionState.IN_GAME), // RequestShapeShiftingItem - Appearance Stone not used on classic
    // TODO NC_GUARD_SEND_DATA_TO_SERVER(0xC8, DiscardPacket::new, ConnectionState.IN_GAME),
    EX_NCGUARD(DiscardPacket::new, ConnectionState.IN_GAME),
    // TODO EX_REQUEST_KALIE_TOKEN(0xC9, null, ConnectionState.IN_GAME),
    EX_REQUEST_KALIE_TOKEN(null, ConnectionState.IN_GAME),
    // TODO EX_REQUEST_SHOW_REGIST_BEAUTY(0xCA, RequestShowBeautyList::new, ConnectionState.IN_GAME),
    EX_REQUEST_SHOW_REGIST_BEAUTY(null, ConnectionState.IN_GAME),
    // TODO EX_REQUEST_REGIST_BEAUTY(0xCB, RequestRegistBeauty::new, ConnectionState.IN_GAME),
    EX_REQUEST_REGIST_BEAUTY(null, ConnectionState.IN_GAME),
    EX_REQUEST_RESET_BEAUTY(null, ConnectionState.IN_GAME),
    // TODO EX_REQUEST_SHOW_RESET_BEAUTY(0xCD, RequestShowResetShopList::new, ConnectionState.IN_GAME),
    EX_REQUEST_SHOW_RESET_BEAUTY(null, ConnectionState.IN_GAME),
    // TODO EX_CHECK_SPEEDHACK(0xCE, null, ConnectionState.IN_GAME),
    EX_CHECK_SPEEDHACK(null, ConnectionState.IN_GAME),
    // TODO EX_BR_ADD_INTERESTED_PRODUCT(0xCF, null, ConnectionState.IN_GAME),
    EX_BR_ADD_INTERESTED_PRODUCT(null, ConnectionState.IN_GAME),
    // TODO EX_BR_DELETE_INTERESTED_PRODUCT(0xD0, null, ConnectionState.IN_GAME),
    EX_BR_DELETE_INTERESTED_PRODUCT(null, ConnectionState.IN_GAME),
    EX_BR_EXIST_NEW_PRODUCT_REQ(null, ConnectionState.IN_GAME), //UNetworkHandler::RequestBR_NewIConCashBtnWnd
    // TODO EX_EVENT_CAMPAIGN_INFO(0xD2, null, ConnectionState.IN_GAME),
    EX_EVENT_CAMPAIGN_INFO(null, ConnectionState.IN_GAME),
    EX_PLEDGE_RECRUIT_INFO(RequestPledgeRecruitInfo::new, ConnectionState.IN_GAME),
    EX_PLEDGE_RECRUIT_BOARD_SEARCH(RequestPledgeRecruitBoardSearch::new, ConnectionState.IN_GAME),
    EX_PLEDGE_RECRUIT_BOARD_APPLY(RequestPledgeRecruitBoardAccess::new, ConnectionState.IN_GAME),
    EX_PLEDGE_RECRUIT_BOARD_DETAIL(RequestPledgeRecruitBoardDetail::new, ConnectionState.IN_GAME),
    EX_PLEDGE_WAITING_LIST_APPLY(RequestPledgeWaitingApply::new, ConnectionState.IN_GAME),
    EX_PLEDGE_WAITING_LIST_APPLIED(RequestPledgeWaitingApplied::new, ConnectionState.IN_GAME),
    EX_PLEDGE_WAITING_LIST(RequestPledgeWaitingList::new, ConnectionState.IN_GAME),
    EX_PLEDGE_WAITING_USER(RequestPledgeWaitingUser::new, ConnectionState.IN_GAME),
    EX_PLEDGE_WAITING_USER_ACCEPT(RequestPledgeWaitingUserAccept::new, ConnectionState.IN_GAME),
    EX_PLEDGE_DRAFT_LIST_SEARCH(RequestPledgeDraftListSearch::new, ConnectionState.IN_GAME),
    EX_PLEDGE_DRAFT_LIST_APPLY(RequestPledgeDraftListApply::new, ConnectionState.IN_GAME),
    EX_PLEDGE_RECRUIT_APPLY_INFO(RequestPledgeRecruitApplyInfo::new, ConnectionState.IN_GAME),
    // TODO EX_PLEDGE_JOIN_SYS(0xDF, null, ConnectionState.IN_GAME),
    EX_PLEDGE_JOIN_SYS(null, ConnectionState.IN_GAME),
    // TODO EX_RESPONSE_WEB_PETITION_ALARM(0xE0, null, ConnectionState.IN_GAME),
    EX_RESPONSE_WEB_PETITION_ALARM(null, ConnectionState.IN_GAME),
    EX_NOTIFY_EXIT_BEAUTYSHOP(NotifyExitBeautyShop::new, ConnectionState.IN_GAME),
    EX_EVENT_REGISTER_XMAS_WISHCARD(null, ConnectionState.IN_GAME),
    EX_ENCHANT_SCROLL_ITEM_ADD(RequestExAddEnchantScrollItem::new, ConnectionState.IN_GAME),
    EX_ENCHANT_SUPPORT_ITEM_REMOVE(RequestExRemoveEnchantSupportItem::new, ConnectionState.IN_GAME),
    EX_SELECT_CARD_REWARD(null, ConnectionState.IN_GAME),
    EX_DIVIDE_ADENA_START(RequestDivideAdenaStart::new, ConnectionState.IN_GAME),
    EX_DIVIDE_ADENA_CANCEL(RequestDivideAdenaCancel::new, ConnectionState.IN_GAME),
    EX_DIVIDE_ADENA(RequestDivideAdena::new, ConnectionState.IN_GAME),
    EX_ACQUIRE_POTENTIAL_SKILL(null, ConnectionState.IN_GAME),
    EX_REQUEST_POTENTIAL_SKILL_LIST(null, ConnectionState.IN_GAME),
    EX_RESET_POTENTIAL_SKILL(null, ConnectionState.IN_GAME),
    EX_CHANGE_POTENTIAL_POINT(null, ConnectionState.IN_GAME),
    EX_STOP_MOVE(RequestStopMove::new, ConnectionState.IN_GAME),
    EX_ABILITY_WND_OPEN(null, ConnectionState.IN_GAME),
    EX_ABILITY_WND_CLOSE(null, ConnectionState.IN_GAME),
    EX_START_LUCKY_GAME(RequestLuckyGameStartInfo::new, ConnectionState.IN_GAME),
    EX_BETTING_LUCKY_GAME(RequestLuckyGamePlay::new, ConnectionState.IN_GAME),
    EX_TRAININGZONE_LEAVING(NotifyTrainingRoomEnd::new, ConnectionState.IN_GAME),
    EX_ENCHANT_ONE(RequestNewEnchantPushOne::new, ConnectionState.IN_GAME),
    EX_ENCHANT_ONE_REMOVE(RequestNewEnchantRemoveOne::new, ConnectionState.IN_GAME),
    EX_ENCHANT_TWO(RequestNewEnchantPushTwo::new, ConnectionState.IN_GAME),
    EX_ENCHANT_TWO_REMOVE(RequestNewEnchantRemoveTwo::new, ConnectionState.IN_GAME),
    EX_ENCHANT_CLOSE(RequestNewEnchantClose::new, ConnectionState.IN_GAME),
    EX_ENCHANT_TRY(RequestNewEnchantTry::new, ConnectionState.IN_GAME),
    // TODO implement packet
    EX_ENCHANT_RETRY_TO_PUT_ITEMS(null, ConnectionState.IN_GAME),
    EX_REQUEST_CARD_REWARD_LIST(null, ConnectionState.IN_GAME), //UNetworkHandler::ExRequestCardRewardList
    EX_REQUEST_ACCOUNT_ATTENDANCE_INFO(null, ConnectionState.IN_GAME), //UNetworkHandler::ExRequestAccountAttendanceInfo
    EX_REQUEST_ACCOUNT_ATTENDANCE_REWARD(null, ConnectionState.IN_GAME), //UNetworkHandler::ExRequestAccountAttendanceReward
    EX_TARGET(RequestTargetActionMenu::new, ConnectionState.IN_GAME), // 196
    EX_SELECTED_QUEST_ZONEID(ExSendSelectedQuestZoneID::new, ConnectionState.IN_GAME),
    EX_ALCHEMY_SKILL_LIST(null, ConnectionState.IN_GAME), // RequestAlchemySkillList not exists on Classic
    EX_TRY_MIX_CUBE(null, ConnectionState.IN_GAME),
    REQUEST_ALCHEMY_CONVERSION(null, ConnectionState.IN_GAME),
    EX_EXECUTED_UIEVENTS_COUNT(null, ConnectionState.IN_GAME),
    // TODO implement packet EX_SEND_CLIENT_INI(0x103, DiscardPacket::new, ConnectionState.AUTHENTICATED),
    EX_CLIENT_INI(DiscardPacket::new, ConnectionState.AUTHENTICATED),
    EX_REQUEST_AUTOFISH(ExRequestAutoFish::new, ConnectionState.IN_GAME),
    EX_REQUEST_VIP_ATTENDANCE_ITEMLIST(RequestVipAttendanceItemList::new, ConnectionState.IN_GAME),
    EX_REQUEST_VIP_ATTENDANCE_CHECK(RequestVipAttendanceCheck::new, ConnectionState.IN_GAME),
    EX_TRY_ENSOUL(RequestItemEnsoul::new, ConnectionState.IN_GAME),
    // TODO implement packet
    EX_CASTLEWAR_SEASON_REWARD(null, ConnectionState.IN_GAME),
    EX_BR_VIP_PRODUCT_LIST_REQ(RequestVipProductList::new, ConnectionState.IN_GAME),
    EX_REQUEST_LUCKY_GAME_INFO(RequestVipLuckyGameInfo::new, ConnectionState.IN_GAME),
    EX_REQUEST_LUCKY_GAME_ITEMLIST(RequestVipLuckyGameItemList::new, ConnectionState.IN_GAME),
    EX_REQUEST_LUCKY_GAME_BONUS(null, ConnectionState.IN_GAME),
    EX_VIP_INFO(ExRequestVipInfo::new, ConnectionState.IN_GAME),
    // TODO REQUEST_CAPTCHA_ANSWER(0x10E, RequestCaptchaAnswer::new, ConnectionState.IN_GAME),
    EX_CAPTCHA_ANSWER(null, ConnectionState.IN_GAME),
    // TODO REQUEST_REFRESH_CAPTCHA_IMAGE(0x10F, RequestRefreshCaptcha::new, ConnectionState.IN_GAME),
    EX_REFRESH_CAPTCHA_IMAGE(null, ConnectionState.IN_GAME),
    EX_PLEDGE_SIGNIN(RequestPledgeSignInForOpenJoiningMethod::new, ConnectionState.IN_GAME),
    EX_REQUEST_MATCH_ARENA(null, ConnectionState.IN_GAME),
    EX_CONFIRM_MATCH_ARENA(null, ConnectionState.IN_GAME),
    EX_CANCEL_MATCH_ARENA(null, ConnectionState.IN_GAME),
    EX_CHANGE_CLASS_ARENA(null, ConnectionState.IN_GAME),
    EX_CONFIRM_CLASS_ARENA(null, ConnectionState.IN_GAME),
    EX_DECO_NPC_INFO(null, ConnectionState.IN_GAME),
    EX_DECO_NPC_SET(null, ConnectionState.IN_GAME),
    EX_FACTION_INFO(null, ConnectionState.IN_GAME),
    EX_EXIT_ARENA(null, ConnectionState.IN_GAME),
    EX_REQUEST_BALTHUS_TOKEN(null, ConnectionState.IN_GAME),
    EX_PARTY_MATCHING_ROOM_HISTORY(null, ConnectionState.IN_GAME),
    EX_ARENA_CUSTOM_NOTIFICATION(null, ConnectionState.IN_GAME),
    EX_TODOLIST(RequestTodoList::new, ConnectionState.IN_GAME, ConnectionState.JOINING_GAME),
    EX_TODOLIST_HTML(null, ConnectionState.IN_GAME),
    EX_ONE_DAY_RECEIVE_REWARD(RequestOneDayRewardReceive::new, ConnectionState.IN_GAME),
    EX_QUEUETICKET(null, ConnectionState.IN_GAME),
    EX_PLEDGE_BONUS_UI_OPEN(RequestPledgeBonusOpen::new, ConnectionState.IN_GAME),
    EX_PLEDGE_BONUS_REWARD_LIST(RequestPledgeBonusRewardList::new, ConnectionState.IN_GAME),
    EX_PLEDGE_BONUS_RECEIVE_REWARD(RequestPledgeBonusReward::new, ConnectionState.IN_GAME),
    EX_SSO_AUTHNTOKEN_REQ(null, ConnectionState.IN_GAME),
    EX_QUEUETICKET_LOGIN(null, ConnectionState.IN_GAME),
    EX_BLOCK_DETAIL_INFO(null, ConnectionState.IN_GAME),
    EX_TRY_ENSOUL_EXTRACTION(RequestTryEnSoulExtraction::new, ConnectionState.IN_GAME),
    EX_RAID_BOSS_SPAWN_INFO(RequestRaidBossSpawnInfo::new, ConnectionState.IN_GAME),
    EX_RAID_SERVER_INFO(RequestRaidServerInfo::new, ConnectionState.IN_GAME),
    EX_SHOW_AGIT_SIEGE_INFO(RequestShowAgitSiegeInfo::new, ConnectionState.IN_GAME),
    EX_ITEM_AUCTION_STATUS(null, ConnectionState.IN_GAME),
    EX_MONSTER_BOOK_OPEN(null, ConnectionState.IN_GAME),
    EX_MONSTER_BOOK_CLOSE(null, ConnectionState.IN_GAME),
    EX_REQ_MONSTER_BOOK_REWARD(null, ConnectionState.IN_GAME),
    EX_MATCHGROUP(null, ConnectionState.IN_GAME),
    EX_MATCHGROUP_ASK(null, ConnectionState.IN_GAME),
    EX_MATCHGROUP_ANSWER(null, ConnectionState.IN_GAME),
    EX_MATCHGROUP_WITHDRAW(null, ConnectionState.IN_GAME),
    EX_MATCHGROUP_OUST(null, ConnectionState.IN_GAME),
    EX_MATCHGROUP_CHANGE_MASTER(null, ConnectionState.IN_GAME),
    EX_UPGRADE_SYSTEM_REQUEST(RequestUpgradeSystemResult::new, ConnectionState.IN_GAME),
    EX_CARD_UPDOWN_PICK_NUMB(null, ConnectionState.IN_GAME),
    EX_CARD_UPDOWN_GAME_REWARD_REQUEST(null, ConnectionState.IN_GAME),
    EX_CARD_UPDOWN_GAME_RETRY(null, ConnectionState.IN_GAME),
    EX_CARD_UPDOWN_GAME_QUIT(null, ConnectionState.IN_GAME),
    EX_ARENA_RANK_ALL(null, ConnectionState.IN_GAME),
    EX_ARENA_MYRANK(null, ConnectionState.IN_GAME),
    EX_SWAP_AGATHION_SLOT_ITEMS(null, ConnectionState.IN_GAME),
    EX_PLEDGE_CONTRIBUTION_RANK(null, ConnectionState.IN_GAME),
    EX_PLEDGE_CONTRIBUTION_INFO(null, ConnectionState.IN_GAME),
    EX_PLEDGE_CONTRIBUTION_REWARD(null, ConnectionState.IN_GAME),
    EX_PLEDGE_LEVEL_UP(null, ConnectionState.IN_GAME),
    EX_PLEDGE_MISSION_INFO(null, ConnectionState.IN_GAME),
    EX_PLEDGE_MISSION_REWARD(null, ConnectionState.IN_GAME),
    EX_PLEDGE_MASTERY_INFO(null, ConnectionState.IN_GAME),
    EX_PLEDGE_MASTERY_SET(null, ConnectionState.IN_GAME),
    EX_PLEDGE_MASTERY_RESET(null, ConnectionState.IN_GAME),
    EX_PLEDGE_SKILL_INFO(null, ConnectionState.IN_GAME),
    EX_PLEDGE_SKILL_ACTIVATE(null, ConnectionState.IN_GAME),
    EX_PLEDGE_ITEM_LIST(null, ConnectionState.IN_GAME),
    EX_PLEDGE_ITEM_ACTIVATE(null, ConnectionState.IN_GAME),
    EX_PLEDGE_ANNOUNCE(null, ConnectionState.IN_GAME),
    EX_PLEDGE_ANNOUNCE_SET(null, ConnectionState.IN_GAME),
    EX_CREATE_PLEDGE(null, ConnectionState.IN_GAME),
    EX_PLEDGE_ITEM_INFO(null, ConnectionState.IN_GAME),
    EX_PLEDGE_ITEM_BUY(null, ConnectionState.IN_GAME),
    EX_ELEMENTAL_SPIRIT_INFO(ExElementalSpiritInfo::new, ConnectionState.IN_GAME),
    EX_ELEMENTAL_SPIRIT_EXTRACT_INFO(ExElementalSpiritExtractInfo::new, ConnectionState.IN_GAME),
    EX_ELEMENTAL_SPIRIT_EXTRACT(ExElementalSpiritExtract::new, ConnectionState.IN_GAME),
    EX_ELEMENTAL_SPIRIT_EVOLUTION_INFO(ExElementalSpiritEvolutionInfo::new, ConnectionState.IN_GAME),
    EX_ELEMENTAL_SPIRIT_EVOLUTION(ExElementalSpiritEvolution::new, ConnectionState.IN_GAME),
    EX_ELEMENTAL_SPIRIT_SET_TALENT(ExElementalSpiritSetTalent::new, ConnectionState.IN_GAME),
    EX_ELEMENTAL_SPIRIT_INIT_TALENT(ExElementalInitTalent::new, ConnectionState.IN_GAME),
    EX_ELEMENTAL_SPIRIT_ABSORB_INFO(ExElementalSpiritAbsorbInfo::new, ConnectionState.IN_GAME),
    EX_ELEMENTAL_SPIRIT_ABSORB(ExElementalSpiritAbsorb::new, ConnectionState.IN_GAME),
    EX_REQUEST_LOCKED_ITEM(null, ConnectionState.IN_GAME),
    EX_REQUEST_UNLOCKED_ITEM(null, ConnectionState.IN_GAME),
    EX_LOCKED_ITEM_CANCEL(null, ConnectionState.IN_GAME),
    EX_UNLOCKED_ITEM_CANCEL(null, ConnectionState.IN_GAME),
    EX_ELEMENTAL_SPIRIT_CHANGE_TYPE(ExElementalSpiritChangeType::new, ConnectionState.IN_GAME),
    // TODO REQUEST_BLOCK_LIST_FOR_AD(0x15D, ExRequestBlockListForAD::new, ConnectionState.IN_GAME),
    REQUEST_BLOCK_LIST_FOR_AD(null, ConnectionState.IN_GAME),
    REQUEST_USER_BAN_INFO(null, ConnectionState.IN_GAME),
    EX_INTERACT_MODIFY(null, ConnectionState.IN_GAME), // 152
    EX_TRY_ENCHANT_ARTIFACT(null, ConnectionState.IN_GAME), // 152
    EX_UPGRADE_SYSTEM_NORMAL_REQUEST(ExUpgradeSystemNormalRequest::new, ConnectionState.IN_GAME), //UNetworkHandler::RequestUpgradeSystemResultN
    EX_PURCHASE_LIMIT_SHOP_ITEM_LIST(null, ConnectionState.IN_GAME), //UNetworkHandler::RequestExPurchaseLimitShopItemList
    EX_PURCHASE_LIMIT_SHOP_ITEM_BUY(null, ConnectionState.IN_GAME), //UNetworkHandler::RequestExPurchaseLimitShopItemBuy
    EX_OPEN_HTML(RequestOpenWndWithoutNPC::new, ConnectionState.IN_GAME), // 196
    EX_REQUEST_CLASS_CHANGE(null, ConnectionState.IN_GAME), // 196
    EX_REQUEST_CLASS_CHANGE_VERIFYING(null, ConnectionState.IN_GAME), // 196
    EX_REQUEST_TELEPORT(RequestExRequestTeleport::new, ConnectionState.IN_GAME), // 196
    // FIXME was removed in 196 ? EX_XIGN_CODE(null, ConnectionState.IN_GAME), // 196
    EX_COSTUME_USE_ITEM(RequestExCostumeUseItem::new, ConnectionState.IN_GAME), // 196
    EX_COSTUME_LIST(RequestExCostumeList::new, ConnectionState.IN_GAME), // 196
    EX_COSTUME_COLLECTION_SKILL_ACTIVE(RequestExCostumeCollectionSkillActive::new, ConnectionState.IN_GAME), // 196
    EX_COSTUME_EVOLUTION(RequestExCostumeEvolution::new, ConnectionState.IN_GAME), // 196
    EX_COSTUME_EXTRACT(RequestExCostumeExtract::new, ConnectionState.IN_GAME), // 196
    EX_COSTUME_LOCK(RequestExCostumeLock::new, ConnectionState.IN_GAME), // 196
    EX_COSTUME_CHANGE_SHORTCUT(RequestExCostumeChangeShortcut::new, ConnectionState.IN_GAME), // 196
    EX_MAGICLAMP_GAME_INFO(null, ConnectionState.IN_GAME), //UNetworkHandler::RequestMagicLampGameInfo
    EX_MAGICLAMP_GAME_START(null, ConnectionState.IN_GAME), //UNetworkHandler::RequestMagicLampGameStart
    EX_ACTIVATE_AUTO_SHORTCUT(CExActivateAutoShortcut::new, ConnectionState.IN_GAME), // 196
    EX_PREMIUM_MANAGER_LINK_HTML(null, ConnectionState.IN_GAME), // 196
    EX_PREMIUM_MANAGER_PASS_CMD_TO_SERVER(null, ConnectionState.IN_GAME), // 196
    EX_ACTIVATED_CURSED_TREASURE_BOX_LOCATION(null, ConnectionState.IN_GAME), // 196
    EX_PAYBACK_LIST(null, ConnectionState.IN_GAME), // 196
    EX_PAYBACK_GIVE_REWARD(null, ConnectionState.IN_GAME), // 196
    EX_AUTOPLAY_SETTING(RequestExAutoplaySetting::new, ConnectionState.IN_GAME), // 196
    EX_FESTIVAL_BM_INFO(null, ConnectionState.IN_GAME), // 196
    EX_FESTIVAL_BM_GAME(null, ConnectionState.IN_GAME), // 196
    EX_RANKING_CHAR_INFO(0x181, null, ConnectionState.IN_GAME),
    EX_RANKING_CHAR_HISTORY(0x182, null, ConnectionState.IN_GAME),
    EX_RANKING_CHAR_RANKERS(0x183, null, ConnectionState.IN_GAME);

    public static final IncomingExPackets[] PACKET_ARRAY;

    static {
        final short maxPacketId = (short) Arrays.stream(values()).mapToInt(IncomingExPackets::getPacketId).max().orElse(0);
        PACKET_ARRAY = new IncomingExPackets[maxPacketId + 1];
        for (IncomingExPackets incomingPacket : values()) {
            PACKET_ARRAY[incomingPacket.getPacketId()] = incomingPacket;
        }
    }

    private final int _id;
    private Supplier<IIncomingPacket<GameClient>> _incomingPacketFactory;
    private Set<IConnectionState> _connectionStates;

    IncomingExPackets(Supplier<IIncomingPacket<GameClient>> incomingPacketFactory, ConnectionState... connectionStates) {
        this(-1, incomingPacketFactory, connectionStates);
    }


    IncomingExPackets(int id, Supplier<IIncomingPacket<GameClient>> incomingPacketFactory, ConnectionState... connectionStates) {
        _id = id > 0 ? id : ordinal();
        // packetId is an unsigned short
        if (id > 0xFFFF) {
            throw new IllegalArgumentException("packetId must not be bigger than 0xFFFF");
        }
        _incomingPacketFactory = incomingPacketFactory != null ? incomingPacketFactory : () -> null;
        _connectionStates = Set.of(connectionStates);
    }

    @Override
    public int getPacketId() {
        return _id;
    }

    @Override
    public IIncomingPacket<GameClient> newIncomingPacket() {
        return _incomingPacketFactory.get();
    }

    @Override
    public Set<IConnectionState> getConnectionStates() {
        return _connectionStates;
    }
}
