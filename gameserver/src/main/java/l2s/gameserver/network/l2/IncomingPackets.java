package l2s.gameserver.network.l2;

import l2s.commons.network.IConnectionState;
import l2s.commons.network.IIncomingPacket;
import l2s.commons.network.IIncomingPackets;
import l2s.gameserver.network.l2.c2s.*;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author UnAfraid
 */
public enum IncomingPackets implements IIncomingPackets<GameClient> {
    LOGOUT(Logout::new, ConnectionState.CONNECTED, ConnectionState.JOINING_GAME, ConnectionState.AUTHENTICATED, ConnectionState.IN_GAME),
    ATTACK(Attack::new, ConnectionState.IN_GAME),
    MOVE_BACKWARD_TO_LOCATION(MoveBackwardToLocation::new, ConnectionState.IN_GAME),
    START_PLEDGE_WAR(RequestStartPledgeWar::new, ConnectionState.IN_GAME),
    NOT_USE_10(null, ConnectionState.IN_GAME),
    STOP_PLEDGE_WAR(RequestStopPledgeWar::new, ConnectionState.IN_GAME),
    NOT_USE_11(null, ConnectionState.IN_GAME),
    NOT_USE_12(null, ConnectionState.IN_GAME),
    NOT_USE_13(null, ConnectionState.IN_GAME),
    SET_PLEDGE_CREST(RequestSetPledgeCrest::new, ConnectionState.IN_GAME),
    NOT_USE_14(null, ConnectionState.IN_GAME),
    GIVE_NICKNAME(RequestGiveNickName::new, ConnectionState.IN_GAME),
    CHARACTER_CREATE(CharacterCreate::new, ConnectionState.AUTHENTICATED),
    CHARACTER_DELETE(CharacterDelete::new, ConnectionState.AUTHENTICATED),
    VERSION(ProtocolVersion::new, ConnectionState.CONNECTED),
    MOVE_TO_LOCATION(MoveBackwardToLocation::new, ConnectionState.IN_GAME),
    NOT_USE_34(null, ConnectionState.IN_GAME),
    ENTER_WORLD(EnterWorld::new, ConnectionState.JOINING_GAME),
    CHARACTER_SELECT(CharacterSelect::new, ConnectionState.AUTHENTICATED),
    NEW_CHARACTER(NewCharacter::new, ConnectionState.AUTHENTICATED),
    ITEMLIST(RequestItemList::new, ConnectionState.IN_GAME),
    NOT_USE_1(null, ConnectionState.IN_GAME),
    UNEQUIP_ITEM(RequestUnEquipItem::new, ConnectionState.IN_GAME),
    DROP_ITEM(RequestDropItem::new, ConnectionState.IN_GAME),
    GET_ITEM(null, ConnectionState.IN_GAME),
    USE_ITEM(UseItem::new, ConnectionState.IN_GAME),
    TRADE_REQUEST(TradeRequest::new, ConnectionState.IN_GAME),
    TRADE_ADD(AddTradeItem::new, ConnectionState.IN_GAME),
    TRADE_DONE(TradeDone::new, ConnectionState.IN_GAME),
    NOT_USE_35(null, ConnectionState.IN_GAME),
    NOT_USE_36(null, ConnectionState.IN_GAME),
    ACTION(Action::new, ConnectionState.IN_GAME),
    NOT_USE_37(null, ConnectionState.IN_GAME),
    NOT_USE_38(null, ConnectionState.IN_GAME),
    LINK_HTML(RequestLinkHtml::new, ConnectionState.IN_GAME),
    PASS_CMD_TO_SERVER(RequestBypassToServer::new, ConnectionState.IN_GAME),
    WRITE_BBS(RequestBBSwrite::new, ConnectionState.IN_GAME),
    NOT_USE_39(null, ConnectionState.IN_GAME),
    JOIN_PLEDGE(RequestJoinPledge::new, ConnectionState.IN_GAME),
    ANSWER_JOIN_PLEDGE(RequestAnswerJoinPledge::new, ConnectionState.IN_GAME),
    WITHDRAWAL_PLEDGE(RequestWithdrawalPledge::new, ConnectionState.IN_GAME),
    OUST_PLEDGE_MEMBER(RequestOustPledgeMember::new, ConnectionState.IN_GAME),
    NOT_USE_40(null, ConnectionState.IN_GAME),
    LOGIN(AuthLogin::new, ConnectionState.CONNECTED),
    GET_ITEM_FROM_PET(RequestGetItemFromPet::new, ConnectionState.IN_GAME),
    NOT_USE_22(null, ConnectionState.IN_GAME),
    ALLIANCE_INFO(RequestAllyInfo::new, ConnectionState.IN_GAME),
    CRYSTALLIZE_ITEM(RequestCrystallizeItem::new, ConnectionState.IN_GAME),
    NOT_USE_19(null, ConnectionState.IN_GAME),
    PRIVATE_STORE_LIST_SET(SetPrivateStoreListSell::new, ConnectionState.IN_GAME),
    PRIVATE_STORE_MANAGE_CANCEL(null, ConnectionState.IN_GAME),
    NOT_USE_41(null, ConnectionState.IN_GAME),
    // TODO check if need REQUEST_TELEPORT(0x33, null, ConnectionState.IN_GAME),
    SOCIAL_ACTION(null, ConnectionState.IN_GAME),
    CHANGE_MOVE_TYPE(null, ConnectionState.IN_GAME),
    CHANGE_WAIT_TYPE(null, ConnectionState.IN_GAME),
    SELL_LIST(RequestSellItem::new, ConnectionState.IN_GAME),
    MAGIC_SKILL_LIST(RequestMagicSkillList::new, ConnectionState.IN_GAME),
    MAGIC_SKILL_USE(RequestMagicSkillUse::new, ConnectionState.IN_GAME),
    APPEARING(Appearing::new, ConnectionState.IN_GAME),
    WAREHOUSE_DEPOSIT_LIST(SendWareHouseDepositList::new, ConnectionState.IN_GAME),
    WAREHOUSE_WITHDRAW_LIST(SendWareHouseWithDrawList::new, ConnectionState.IN_GAME),
    SHORTCUT_REG(RequestShortCutReg::new, ConnectionState.IN_GAME),
    NOT_USE_3(null, ConnectionState.IN_GAME),
    DEL_SHORTCUT(RequestShortCutDel::new, ConnectionState.IN_GAME),
    BUY_LIST(RequestBuyItem::new, ConnectionState.IN_GAME),
    NOT_USE_2(null, ConnectionState.IN_GAME),
    JOIN_PARTY(RequestJoinParty::new, ConnectionState.IN_GAME),
    ANSWER_JOIN_PARTY(RequestAnswerJoinParty::new, ConnectionState.IN_GAME),
    WITHDRAWAL_PARTY(RequestWithDrawalParty::new, ConnectionState.IN_GAME),
    OUST_PARTY_MEMBER(RequestOustPartyMember::new, ConnectionState.IN_GAME),
    DISMISS_PARTY(null, ConnectionState.IN_GAME), //UNe
    CANNOT_MOVE_ANYMORE(CannotMoveAnymore::new, ConnectionState.IN_GAME),
    TARGET_UNSELECTED(RequestTargetCanceld::new, ConnectionState.IN_GAME),
    SAY2(Say2::new, ConnectionState.IN_GAME),
    NOT_USE_42(null, ConnectionState.IN_GAME),
    NOT_USE_4(null, ConnectionState.IN_GAME),
    NOT_USE_5(null, ConnectionState.IN_GAME),
    PLEDGE_REQ_SHOW_MEMBER_LIST_OPEN(RequestPledgeMemberList::new, ConnectionState.IN_GAME),
    NOT_USE_6(null, ConnectionState.IN_GAME),
    NOT_USE_7(null, ConnectionState.IN_GAME),
    SKILL_LIST(RequestSkillList::new, ConnectionState.IN_GAME),
    NOT_USE_8(null, ConnectionState.IN_GAME),
    MOVE_WITH_DELTA(MoveWithDelta::new, ConnectionState.IN_GAME),
    GETON_VEHICLE(RequestGetOnVehicle::new, ConnectionState.IN_GAME),
    GETOFF_VEHICLE(RequestGetOffVehicle::new, ConnectionState.IN_GAME),
    TRADE_START(AnswerTradeRequest::new, ConnectionState.IN_GAME),
    ICON_ACTION(RequestActionUse::new, ConnectionState.IN_GAME),
    RESTART(RequestRestart::new, ConnectionState.IN_GAME),
    NOT_USE_9(null, ConnectionState.IN_GAME),
    VALIDATE_POSITION(ValidatePosition::new, ConnectionState.IN_GAME),
    SEK_COSTUME(null, ConnectionState.IN_GAME), //UNe
    START_ROTATING(StartRotating::new, ConnectionState.IN_GAME),
    FINISH_ROTATING(FinishRotating::new, ConnectionState.IN_GAME),
    NOT_USE_15(null, ConnectionState.IN_GAME),
    SHOW_BOARD(RequestShowBoard::new, ConnectionState.IN_GAME),
    REQUEST_ENCHANT_ITEM(RequestEnchantItem::new, ConnectionState.IN_GAME),
    DESTROY_ITEM(RequestDestroyItem::new, ConnectionState.IN_GAME),
    TARGET_USER_FROM_MENU(null, ConnectionState.IN_GAME),
    QUESTLIST(RequestQuestList::new, ConnectionState.IN_GAME),
    DESTROY_QUEST(RequestQuestAbort::new, ConnectionState.IN_GAME),
    NOT_USE_16(null, ConnectionState.IN_GAME),
    PLEDGE_INFO(RequestPledgeInfo::new, ConnectionState.IN_GAME),
    PLEDGE_EXTENDED_INFO(RequestPledgeExtendedInfo::new, ConnectionState.IN_GAME),
    PLEDGE_CREST(RequestPledgeCrest::new, ConnectionState.IN_GAME),
    NOT_USE_17(null, ConnectionState.IN_GAME),
    NOT_USE_18(null, ConnectionState.IN_GAME),
    L2_FRIEND_LIST(RequestFriendInfoList::new, ConnectionState.IN_GAME),
    L2_FRIEND_SAY(RequestSendFriendMsg::new, ConnectionState.IN_GAME),
    OPEN_MINIMAP(RequestShowMiniMap::new, ConnectionState.IN_GAME),
    MSN_CHAT_LOG(RequestSendMsnChatLog::new, ConnectionState.IN_GAME),
    RELOAD(RequestRecordInfo::new, ConnectionState.IN_GAME),
    HENNA_EQUIP(RequestHennaEquip::new, ConnectionState.IN_GAME),
    HENNA_UNEQUIP_LIST(RequestHennaRemoveList::new, ConnectionState.IN_GAME),
    HENNA_UNEQUIP_INFO(RequestHennaItemRemoveInfo::new, ConnectionState.IN_GAME),
    HENNA_UNEQUIP(RequestHennaRemove::new, ConnectionState.IN_GAME),
    ACQUIRE_SKILL_INFO(RequestAcquireSkillInfo::new, ConnectionState.IN_GAME),
    SYS_CMD_2(SendBypassBuildCmd::new, ConnectionState.IN_GAME),
    MOVE_TO_LOCATION_IN_VEHICLE(RequestMoveToLocationInVehicle::new, ConnectionState.IN_GAME),
    CAN_NOT_MOVE_ANYMORE_IN_VEHICLE(CannotMoveAnymoreInVehicle::new, ConnectionState.IN_GAME),
    FRIEND_ADD_REQUEST(RequestFriendInvite::new, ConnectionState.IN_GAME),
    FRIEND_ADD_REPLY(RequestAnswerFriendInvite::new, ConnectionState.IN_GAME),
    FRIEND_LIST(RequestFriendList::new, ConnectionState.IN_GAME),
    FRIEND_REMOVE(RequestFriendDel::new, ConnectionState.IN_GAME),
    RESTORE_CHARACTER(CharacterRestore::new, ConnectionState.AUTHENTICATED),
    REQ_ACQUIRE_SKILL(RequestAcquireSkill::new, ConnectionState.IN_GAME),
    RESTART_POINT(RequestRestartPoint::new, ConnectionState.IN_GAME),
    GM_COMMAND_TYPE(RequestGMCommand::new, ConnectionState.IN_GAME),
    LIST_PARTY_WAITING(RequestPartyMatchConfig::new, ConnectionState.IN_GAME),
    MANAGE_PARTY_ROOM(RequestPartyMatchList::new, ConnectionState.IN_GAME),
    JOIN_PARTY_ROOM(RequestPartyMatchDetail::new, ConnectionState.IN_GAME),
    NOT_USE_20(RequestPrivateStoreList::new, ConnectionState.IN_GAME),
    PRIVATE_STORE_BUY_LIST_SEND(RequestPrivateStoreBuy::new, ConnectionState.IN_GAME),
    NOT_USE_21(null, ConnectionState.IN_GAME),
    TUTORIAL_LINK_HTML(RequestTutorialLinkHtml::new, ConnectionState.IN_GAME),
    TUTORIAL_PASS_CMD_TO_SERVER(RequestTutorialPassCmdToServer::new, ConnectionState.IN_GAME),
    TUTORIAL_MARK_PRESSED(RequestTutorialQuestionMark::new, ConnectionState.IN_GAME),
    TUTORIAL_CLIENT_EVENT(RequestTutorialClientEvent::new, ConnectionState.IN_GAME),
    PETITION(RequestPetition::new, ConnectionState.IN_GAME),
    PETITION_CANCEL(RequestPetitionCancel::new, ConnectionState.IN_GAME),
    GMLIST(RequestGmList::new, ConnectionState.IN_GAME),
    JOIN_ALLIANCE(RequestJoinAlly::new, ConnectionState.IN_GAME),
    ANSWER_JOIN_ALLIANCE(RequestAnswerJoinAlly::new, ConnectionState.IN_GAME),
    WITHDRAW_ALLIANCE(AllyLeave::new, ConnectionState.IN_GAME),
    OUST_ALLIANCE_MEMBER_PLEDGE(AllyDismiss::new, ConnectionState.IN_GAME),
    DISMISS_ALLIANCE(RequestDismissAlly::new, ConnectionState.IN_GAME),
    SET_ALLIANCE_CREST(RequestSetAllyCrest::new, ConnectionState.IN_GAME),
    ALLIANCE_CREST(RequestAllyCrest::new, ConnectionState.IN_GAME),
    CHANGE_PET_NAME(RequestChangePetName::new, ConnectionState.IN_GAME),
    PET_USE_ITEM(RequestPetUseItem::new, ConnectionState.IN_GAME),
    GIVE_ITEM_TO_PET(RequestGiveItemToPet::new, ConnectionState.IN_GAME),
    PRIVATE_STORE_QUIT(RequestPrivateStoreQuitSell::new, ConnectionState.IN_GAME),
    PRIVATE_STORE_SET_MSG(SetPrivateStoreMsgSell::new, ConnectionState.IN_GAME),
    PET_GET_ITEM(RequestPetGetItem::new, ConnectionState.IN_GAME),
    NOT_USE_23(null, ConnectionState.IN_GAME),
    PRIVATE_STORE_BUY_LIST_SET(SetPrivateStoreListBuy::new, ConnectionState.IN_GAME),
    PRIVATE_STORE_BUY_MANAGE_CANCEL(RequestPrivateStoreBuyManageCancel::new, ConnectionState.IN_GAME),
    PRIVATE_STORE_BUY_QUIT(RequestPrivateStoreQuitBuy::new, ConnectionState.IN_GAME),
    PRIVATE_STORE_BUY_SET_MSG(SetPrivateStoreMsgBuy::new, ConnectionState.IN_GAME),
    NOT_USE_24(RequestTimeCheck::new, ConnectionState.IN_GAME),
    PRIVATE_STORE_BUY_BUY_LIST_SEND(RequestPrivateStoreSell::new, ConnectionState.IN_GAME),  //UNetworkHandler::SendPrivateStoreBuyBuyList
    NOT_USE_25(null, ConnectionState.IN_GAME),  //UNetworkHandler::
    NOT_USE_26(null, ConnectionState.IN_GAME),  //UNetworkHandler::
    NOT_USE_27(null, ConnectionState.IN_GAME),  //UNetworkHandler::
    NOT_USE_28(null, ConnectionState.IN_GAME),  //UNetworkHandler::
    NOT_USE_29(null, ConnectionState.IN_GAME),  //UNetworkHandler::
    NOT_USE_30(null, ConnectionState.IN_GAME),  //UNetworkHandler::
    REQUEST_SKILL_COOL_TIME(RequestSkillCoolTime::new, ConnectionState.JOINING_GAME, ConnectionState.IN_GAME),
    REQUEST_PACKAGE_SENDABLE_ITEM_LIST(RequestPackageSendableItemList::new, ConnectionState.IN_GAME),
    REQUEST_PACKAGE_SEND(RequestPackageSend::new, ConnectionState.IN_GAME),
    BLOCK_PACKET(RequestBlock::new, ConnectionState.IN_GAME),
    CASTLE_SIEGE_INFO(RequestSiegeInfo::new, ConnectionState.IN_GAME),
    CASTLE_SIEGE_ATTACKER_LIST(RequestSiegeAttackerList::new, ConnectionState.IN_GAME),
    CASTLE_SIEGE_DEFENDER_LIST(RequestSiegeDefenderList::new, ConnectionState.IN_GAME),
    JOIN_CASTLE_SIEGE(RequestJoinSiege::new, ConnectionState.IN_GAME),
    CONFIRM_CASTLE_SIEGE_WAITING_LIST(RequestConfirmSiegeWaitingList::new, ConnectionState.IN_GAME),
    SET_CASTLE_SIEGE_TIME(RequestSetCastleSiegeTime::new, ConnectionState.IN_GAME),
    MULTI_SELL_CHOOSE(MultiSellChoose::new, ConnectionState.IN_GAME),
    NET_PING(null, ConnectionState.IN_GAME),
    REMAIN_TIME(null, ConnectionState.IN_GAME),
    USER_CMD_BYPASS(BypassUserCmd::new, ConnectionState.IN_GAME),
    SNOOP_QUIT(SnoopQuit::new, ConnectionState.IN_GAME),
    RECIPE_BOOK_OPEN(RequestRecipeBookOpen::new, ConnectionState.IN_GAME),
    RECIPE_ITEM_DELETE(RequestRecipeBookDestroy::new, ConnectionState.IN_GAME),
    RECIPE_ITEM_MAKE_INFO(RequestRecipeItemMakeInfo::new, ConnectionState.IN_GAME),
    RECIPE_ITEM_MAKE_SELF(RequestRecipeItemMakeSelf::new, ConnectionState.IN_GAME),
    NOT_USE_31(null, ConnectionState.IN_GAME),
    RECIPE_SHOP_MESSAGE_SET(RequestRecipeShopMessageSet::new, ConnectionState.IN_GAME),
    RECIPE_SHOP_LIST_SET(RequestRecipeShopListSet::new, ConnectionState.IN_GAME),
    RECIPE_SHOP_MANAGE_QUIT(RequestRecipeShopManageQuit::new, ConnectionState.IN_GAME),
    RECIPE_SHOP_MANAGE_CANCEL(null, ConnectionState.IN_GAME),
    RECIPE_SHOP_MAKE_INFO(RequestRecipeShopMakeInfo::new, ConnectionState.IN_GAME),
    RECIPE_SHOP_MAKE_DO(RequestRecipeShopMakeItem::new, ConnectionState.IN_GAME),
    RECIPE_SHOP_SELL_LIST(RequestRecipeShopManagePrev::new, ConnectionState.IN_GAME),
    OBSERVER_END(ObserverReturn::new, ConnectionState.IN_GAME),
    VOTE_SOCIALITY(null, ConnectionState.IN_GAME),
    HENNA_ITEM_LIST(RequestHennaItemList::new, ConnectionState.IN_GAME),
    HENNA_ITEM_INFO(RequestHennaItemInfo::new, ConnectionState.IN_GAME),
    BUY_SEED(null, ConnectionState.IN_GAME),
    CONFIRM_DLG(DlgAnswer::new, ConnectionState.IN_GAME),
    BUY_PREVIEW_LIST(RequestPreviewItem::new, ConnectionState.IN_GAME),
    SSQ_STATUS(RequestSSQStatus::new, ConnectionState.IN_GAME),
    PETITION_VOTE(RequestPetitionFeedback::new, ConnectionState.IN_GAME),
    NOT_USE_33(null, ConnectionState.IN_GAME),
    GAME_GUARD_REPLY(GameGuardReply::new, ConnectionState.IN_GAME),
    MANAGE_PLEDGE_POWER(RequestPledgePower::new, ConnectionState.IN_GAME),
    MAKE_MACRO(RequestMakeMacro::new, ConnectionState.IN_GAME),
    DELETE_MACRO(RequestDeleteMacro::new, ConnectionState.IN_GAME),
    NOT_USE_32(null, ConnectionState.IN_GAME),
    EX_PACKET(ExPacket::new, ConnectionState.values()); // This packet has its own connection state checking so we allow all of them

    public static final IncomingPackets[] PACKET_ARRAY;

    static {
        final short maxPacketId = (short) Arrays.stream(values()).mapToInt(IncomingPackets::getPacketId).max().orElse(0);
        PACKET_ARRAY = new IncomingPackets[maxPacketId + 1];
        for (IncomingPackets incomingPacket : values()) {
            PACKET_ARRAY[incomingPacket.getPacketId()] = incomingPacket;
        }
    }

    private Supplier<IIncomingPacket<GameClient>> _incomingPacketFactory;
    private Set<IConnectionState> _connectionStates;

    IncomingPackets(Supplier<IIncomingPacket<GameClient>> incomingPacketFactory, IConnectionState... connectionStates) {
        // packetId is an unsigned byte
        if (ordinal() > 0xFF) {
            throw new IllegalArgumentException("packetId must not be bigger than 0xFF");
        }
        _incomingPacketFactory = incomingPacketFactory != null ? incomingPacketFactory : () -> null;
        _connectionStates = Set.of(connectionStates);
    }

    @Override
    public int getPacketId() {
        return ordinal();
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
