package l2s.gameserver.network.l2;

import l2s.commons.net.nio.impl.*;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.network.l2.c2s.*;
import l2s.gameserver.network.l2.c2s.augmentation.*;
import l2s.gameserver.network.l2.c2s.basket.RequestBR_AddBasketProductInfo;
import l2s.gameserver.network.l2.c2s.basket.RequestBR_DeleteBasketProductInfo;
import l2s.gameserver.network.l2.c2s.coupon.RequestPCCafeCouponUse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public final class GamePacketHandler implements IPacketHandler<GameClient>, IClientFactory<GameClient>, IMMOExecutor<GameClient>
{
	private static final Logger _log;

	@Override
	public ReceivablePacket<GameClient> handlePacket(ByteBuffer buf, GameClient client)
	{
		int id = buf.get() & 0xFF;
		ReceivablePacket<GameClient> msg = null;
		try
		{
			//			System.out.println("client: " + id);
            main:
			{
				switch(client.getState())
				{
					case CONNECTED:
					{
						switch(id)
						{
							case 0x00:
							{
								msg = new RequestStatus();
								break main;
							}
							case 14:
							{
								msg = new ProtocolVersion();
								break main;
							}
							case 43:
							{
								msg = new AuthLogin();
								break main;
							}
							case 203:
							{
								msg = new ReplyGameGuardQuery();
								break main;
							}
							case 208:
							{
								int id3 = buf.getShort() & 65535;
								if(id3 == 292)
								{
									//									msg = new RequestQueueTicketLogin();
								}

								break main;
							}
							default:
							{
								client.onUnknownPacket();
								_log.warn("Unknown client packet! State: CONNECTED, packet ID: " + Integer.toHexString(id).toUpperCase());
								break main;
							}
						}
					}
					case AUTHED:
					{
						switch(id)
						{
							case 0:
							{
								msg = new Logout();
								break main;
							}
							case 12:
							{
								msg = new CharacterCreate();
								break main;
							}
							case 13:
							{
								msg = new CharacterDelete();
								break main;
							}
							case 18:
							{
								msg = new CharacterSelected();
								break main;
							}
							case 19:
							{
								msg = new NewCharacter();
								break main;
							}
							case 123:
							{
								msg = new CharacterRestore();
								break main;
							}
							case 177:
							{
								msg = new NetPing();
								break main;
							}
							case 203:
							{
								msg = new ReplyGameGuardQuery();
								break main;
							}
							case 208:
							{
								int id3 = buf.getShort() & 0xFFFF;
								switch(id3)
								{
									case 1:
									{
										msg = new RequestManorList();
										break main;
									}
									case 33:
									{
										msg = new RequestKeyMapping();
										break main;
									}
									case 51:
									{
										msg = new GotoLobby();
										break main;
									}
									case 58:
									{
										break main;
									}
									case 166:
									{
										msg = new RequestEx2ndPasswordCheck();
										break main;
									}
									case 167:
									{
										msg = new RequestEx2ndPasswordVerify();
										break main;
									}
									case 168:
									{
										msg = new RequestEx2ndPasswordReq();
										break main;
									}
									case 169:
									{
										msg = new RequestCharacterNameCreatable();
										break main;
									}
									case 209:
									{
										msg = new RequestBR_NewIConCashBtnWnd();
										break main;
									}
									case 259:
									{
										break main;
									}
                                    case 0x104: {
                                        // TODO D0	104	C_EX_CLIENT_INI
                                        break main;
                                    }
									default:
									{
										client.onUnknownPacket();
										_log.warn("Unknown client packet! State: AUTHED, packet ID: " + Integer.toHexString(id).toUpperCase() + ":" + Integer.toHexString(id3).toUpperCase());
										break main;
									}
								}
							}
							default:
							{
								client.onUnknownPacket();
								_log.warn("Unknown client packet! State: AUTHE, packet ID: " + Integer.toHexString(id).toUpperCase());
								break main;
							}
						}
					}
					case IN_GAME:
					{
						switch(id)
						{
							case 0:
							{
								msg = new Logout();
								break main;
							}
							case 1:
							{
								msg = new AttackRequest();
								break main;
							}
							case 2:
							{
								break main;
							}
							case 3:
							{
								msg = new RequestStartPledgeWar();
								break main;
							}
							case 4:
							{
								msg = new RequestReplyStartPledgeWar();
								break main;
							}
							case 5:
							{
								msg = new RequestStopPledgeWar();
								break main;
							}
							case 6:
							{
								msg = new RequestReplyStopPledgeWar();
								break main;
							}
							case 7:
							{
								msg = new RequestSurrenderPledgeWar();
								break main;
							}
							case 8:
							{
								msg = new RequestReplySurrenderPledgeWar();
								break main;
							}
							case 9:
							{
								msg = new RequestSetPledgeCrest();
								break main;
							}
							case 10:
							{
								break main;
							}
							case 11:
							{
								msg = new RequestGiveNickName();
								break main;
							}
							case 12:
							{
								break main;
							}
							case 13:
							{
								break main;
							}
							case 15:
							{
								msg = new MoveBackwardToLocation();
								break main;
							}
							case 16:
							{
								break main;
							}
							case 17:
							{
								msg = new EnterWorld();
								break main;
							}
							case 18:
							{
								break main;
							}
							case 20:
							{
								msg = new RequestItemList();
								break main;
							}
							case 21:
							{
								break main;
							}
							case 22:
							{
								break main;
							}
							case 23:
							{
								msg = new RequestDropItem();
								break main;
							}
							case 24:
							{
								break main;
							}
							case 25:
							{
								msg = new UseItem();
								break main;
							}
							case 26:
							{
								msg = new TradeRequest();
								break main;
							}
							case 27:
							{
								msg = new AddTradeItem();
								break main;
							}
							case 28:
							{
								msg = new TradeDone();
								break main;
							}
							case 29:
							{
								break main;
							}
							case 30:
							{
								break main;
							}
							case 31:
							{
								msg = new Action();
								break main;
							}
							case 32:
							{
								break main;
							}
							case 33:
							{
								break main;
							}
							case 34:
							{
								msg = new RequestLinkHtml();
								break main;
							}
							case 35:
							{
								msg = new RequestBypassToServer();
								break main;
							}
							case 36:
							{
								msg = new RequestBBSwrite();
								break main;
							}
							case 37:
							{
								msg = new RequestCreatePledge();
								break main;
							}
							case 38:
							{
								msg = new RequestJoinPledge();
								break main;
							}
							case 39:
							{
								msg = new RequestAnswerJoinPledge();
								break main;
							}
							case 40:
							{
								msg = new RequestWithdrawalPledge();
								break main;
							}
							case 41:
							{
								msg = new RequestOustPledgeMember();
								break main;
							}
							case 42:
							{
								break main;
							}
							case 44:
							{
								msg = new RequestGetItemFromPet();
								break main;
							}
							case 45:
							{
								break main;
							}
							case 46:
							{
								msg = new RequestAllyInfo();
								break main;
							}
							case 47:
							{
								msg = new RequestCrystallizeItem();
								break main;
							}
							case 48:
							{
								break main;
							}
							case 49:
							{
								msg = new SetPrivateStoreSellList();
								break main;
							}
							case 50:
							{
								break main;
							}
							case 51:
							{
								msg = new RequestTeleport();
								break main;
							}
							case 52:
							{
								break main;
							}
							case 53:
							{
								break main;
							}
							case 54:
							{
								break main;
							}
							case 55:
							{
								msg = new RequestSellItem();
								break main;
							}
							case 56:
							{
								msg = new RequestMagicSkillList();
								break main;
							}
							case 57:
							{
								msg = new RequestMagicSkillUse();
								break main;
							}
							case 58:
							{
								msg = new Appearing();
								break main;
							}
							case 59:
							{
								if(Config.ALLOW_WAREHOUSE)
								{
									msg = new SendWareHouseDepositList();
									break main;
								}
								break main;
							}
							case 60:
							{
								msg = new SendWareHouseWithDrawList();
								break main;
							}
							case 61:
							{
								msg = new RequestShortCutReg();
								break main;
							}
							case 62:
							{
								break main;
							}
							case 63:
							{
								msg = new RequestShortCutDel();
								break main;
							}
							case 64:
							{
								msg = new RequestBuyItem();
								break main;
							}
							case 65:
							{
								break main;
							}
							case 66:
							{
								msg = new RequestJoinParty();
								break main;
							}
							case 67:
							{
								msg = new RequestAnswerJoinParty();
								break main;
							}
							case 68:
							{
								msg = new RequestWithDrawalParty();
								break main;
							}
							case 69:
							{
								msg = new RequestOustPartyMember();
								break main;
							}
							case 70:
							{
								msg = new RequestDismissParty();
								break main;
							}
							case 71:
							{
								msg = new CannotMoveAnymore();
								break main;
							}
							case 72:
							{
								msg = new RequestTargetCanceld();
								break main;
							}
							case 73:
							{
								msg = new Say2C();
								break main;
							}
							case 74:
							{
                                int id2 = buf.get() & 0xFF;
                                switch(id2)
								{
									case 0:
									{
										break main;
									}
									case 1:
									{
										break main;
									}
									case 2:
									{
										break main;
									}
									case 3:
									{
										break main;
									}
									default:
									{
										client.onUnknownPacket();
										_log.warn("Unknown client packet! State: IN_GAME, packet ID: " + Integer.toHexString(id).toUpperCase() + ":" + Integer.toHexString(id2).toUpperCase());
										break main;
									}
								}
							}
							case 75:
							{
								break main;
							}
							case 76:
							{
								break main;
							}
							case 77:
							{
								msg = new RequestPledgeMemberList();
								break main;
							}
							case 78:
							{
								break main;
							}
							case 79:
							{
								break main;
							}
							case 80:
							{
								msg = new RequestSkillList();
								break main;
							}
							case 81:
							{
								break main;
							}
							case 82:
							{
								msg = new MoveWithDelta();
								break main;
							}
							case 83:
							{
								msg = new RequestGetOnVehicle();
								break main;
							}
							case 84:
							{
								msg = new RequestGetOffVehicle();
								break main;
							}
							case 85:
							{
								msg = new AnswerTradeRequest();
								break main;
							}
							case 86:
							{
								msg = new RequestActionUse();
								break main;
							}
							case 87:
							{
								msg = new RequestRestart();
								break main;
							}
							case 88:
							{
								msg = new RequestSiegeInfo();
								break main;
							}
							case 89:
							{
								msg = new ValidatePosition();
								break main;
							}
							case 90:
							{
								msg = new RequestSEKCustom();
								break main;
							}
							case 91:
							{
								msg = new StartRotatingC();
								break main;
							}
							case 92:
							{
								msg = new FinishRotatingC();
								break main;
							}
							case 93:
							{
								break main;
							}
							case 94:
							{
								msg = new RequestShowBoard();
								break main;
							}
							case 95:
							{
								msg = new RequestEnchantItem();
								break main;
							}
							case 96:
							{
								msg = new RequestDestroyItem();
								break main;
							}
							case 97:
							{
								break main;
							}
							case 98:
							{
								msg = new RequestQuestList();
								break main;
							}
							case 99:
							{
								msg = new RequestQuestAbort();
								break main;
							}
							case 100:
							{
								break main;
							}
							case 101:
							{
								msg = new RequestPledgeInfo();
								break main;
							}
							case 102:
							{
								msg = new RequestPledgeExtendedInfo();
								break main;
							}
							case 103:
							{
								msg = new RequestPledgeCrest();
								break main;
							}
							case 104:
							{
								break main;
							}
							case 105:
							{
								break main;
							}
							case 106:
							{
								msg = new RequestFriendInfoList();
								break main;
							}
							case 107:
							{
								msg = new RequestSendL2FriendSay();
								break main;
							}
							case 108:
							{
								msg = new RequestShowMiniMap();
								break main;
							}
							case 109:
							{
								msg = new RequestSendMsnChatLog();
								break main;
							}
							case 110:
							{
								msg = new RequestReload();
								break main;
							}
							case 111:
							{
								msg = new RequestHennaEquip();
								break main;
							}
							case 112:
							{
								msg = new RequestHennaUnequipList();
								break main;
							}
							case 113:
							{
								msg = new RequestHennaUnequipInfo();
								break main;
							}
							case 114:
							{
								msg = new RequestHennaUnequip();
								break main;
							}
							case 115:
							{
								msg = new RequestAquireSkillInfo();
								break main;
							}
							case 116:
							{
								msg = new SendBypassBuildCmd();
								break main;
							}
							case 117:
							{
								msg = new RequestMoveToLocationInVehicle();
								break main;
							}
							case 118:
							{
								msg = new CannotMoveAnymoreInVehicle();
								break main;
							}
							case 119:
							{
								msg = new RequestFriendInvite();
								break main;
							}
							case 120:
							{
								msg = new RequestFriendAddReply();
								break main;
							}
							case 121:
							{
								break main;
							}
							case 122:
							{
								msg = new RequestFriendDel();
								break main;
							}
							case 124:
							{
								msg = new RequestAquireSkill();
								break main;
							}
							case 125:
							{
								msg = new RequestRestartPoint();
								break main;
							}
							case 126:
							{
								msg = new RequestGMCommand();
								break main;
							}
							case 127:
							{
								msg = new RequestPartyMatchConfig();
								break main;
							}
							case 128:
							{
								msg = new RequestPartyMatchList();
								break main;
							}
							case 129:
							{
								msg = new RequestPartyMatchDetail();
								break main;
							}
							case 130:
							{
								msg = new RequestPrivateStoreList();
								break main;
							}
							case 131:
							{
								msg = new RequestPrivateStoreBuy();
								break main;
							}
							case 132:
							{
								break main;
							}
							case 133:
							{
								msg = new RequestTutorialLinkHtml();
								break main;
							}
							case 134:
							{
								msg = new RequestTutorialPassCmdToServer();
								break main;
							}
							case 135:
							{
								msg = new RequestTutorialQuestionMark();
								break main;
							}
							case 136:
							{
								msg = new RequestTutorialClientEvent();
								break main;
							}
							case 137:
							{
								msg = new RequestPetition();
								break main;
							}
							case 138:
							{
								msg = new RequestPetitionCancel();
								break main;
							}
							case 139:
							{
								msg = new RequestGmList();
								break main;
							}
							case 140:
							{
								msg = new RequestJoinAlly();
								break main;
							}
							case 141:
							{
								msg = new RequestAnswerJoinAlly();
								break main;
							}
							case 142:
							{
								msg = new RequestWithdrawAlly();
								break main;
							}
							case 143:
							{
								msg = new RequestOustAlly();
								break main;
							}
							case 144:
							{
								msg = new RequestDismissAlly();
								break main;
							}
							case 145:
							{
								msg = new RequestSetAllyCrest();
								break main;
							}
							case 146:
							{
								msg = new RequestAllyCrest();
								break main;
							}
							case 147:
							{
								msg = new RequestChangePetName();
								break main;
							}
							case 148:
							{
								msg = new RequestPetUseItem();
								break main;
							}
							case 149:
							{
								msg = new RequestGiveItemToPet();
								break main;
							}
							case 150:
							{
								msg = new RequestPrivateStoreQuitSell();
								break main;
							}
							case 151:
							{
								msg = new SetPrivateStoreMsgSell();
								break main;
							}
							case 152:
							{
								msg = new RequestPetGetItem();
								break main;
							}
							case 153:
							{
								msg = new RequestPrivateStoreBuyManage();
								break main;
							}
							case 154:
							{
								msg = new SetPrivateStoreBuyList();
								break main;
							}
							case 155:
							{
								break main;
							}
							case 156:
							{
								msg = new RequestPrivateStoreQuitBuy();
								break main;
							}
							case 157:
							{
								msg = new SetPrivateStoreMsgBuy();
								break main;
							}
							case 158:
							{
								break main;
							}
							case 159:
							{
								msg = new RequestPrivateStoreBuySellList();
								break main;
							}
							case 160:
							{
								msg = new RequestTimeCheck();
								break main;
							}
							case 161:
							{
								break main;
							}
							case 162:
							{
								break main;
							}
							case 163:
							{
								break main;
							}
							case 164:
							{
								break main;
							}
							case 165:
							{
								break main;
							}
							case 166:
							{
								msg = new RequestSkillCoolTime();
								break main;
							}
							case 167:
							{
								msg = new RequestPackageSendableItemList();
								break main;
							}
							case 168:
							{
								msg = new RequestPackageSend();
								break main;
							}
							case 169:
							{
								msg = new RequestBlock();
								break main;
							}
							case 170:
							{
								break main;
							}
							case 171:
							{
								msg = new RequestCastleSiegeAttackerList();
								break main;
							}
							case 172:
							{
								msg = new RequestCastleSiegeDefenderList();
								break main;
							}
							case 173:
							{
								msg = new RequestJoinCastleSiege();
								break main;
							}
							case 174:
							{
								msg = new RequestConfirmCastleSiegeWaitingList();
								break main;
							}
							case 175:
							{
								msg = new RequestSetCastleSiegeTime();
								break main;
							}
							case 176:
							{
								msg = new RequestMultiSellChoose();
								break main;
							}
							case 177:
							{
								msg = new NetPing();
								break main;
							}
							case 178:
							{
								msg = new RequestRemainTime();
								break main;
							}
							case 179:
							{
								msg = new BypassUserCmd();
								break main;
							}
							case 180:
							{
								msg = new SnoopQuit();
								break main;
							}
							case 181:
							{
								msg = new RequestRecipeBookOpen();
								break main;
							}
							case 182:
							{
								msg = new RequestRecipeItemDelete();
								break main;
							}
							case 183:
							{
								msg = new RequestRecipeItemMakeInfo();
								break main;
							}
							case 184:
							{
								msg = new RequestRecipeItemMakeSelf();
								break main;
							}
							case 185:
							{
								break main;
							}
							case 186:
							{
								msg = new RequestRecipeShopMessageSet();
								break main;
							}
							case 187:
							{
								msg = new RequestRecipeShopListSet();
								break main;
							}
							case 188:
							{
								msg = new RequestRecipeShopManageQuit();
								break main;
							}
							case 189:
							{
								msg = new RequestRecipeShopManageCancel();
								break main;
							}
							case 190:
							{
								msg = new RequestRecipeShopMakeInfo();
								break main;
							}
							case 191:
							{
								msg = new RequestRecipeShopMakeDo();
								break main;
							}
							case 192:
							{
								msg = new RequestRecipeShopSellList();
								break main;
							}
							case 193:
							{
								msg = new RequestObserverEnd();
								break main;
							}
							case 194:
							{
								break main;
							}
							case 195:
							{
								msg = new RequestHennaList();
								break main;
							}
							case 196:
							{
								msg = new RequestHennaItemInfo();
								break main;
							}
							case 197:
							{
								msg = new RequestBuySeed();
								break main;
							}
							case 198:
							{
								msg = new ConfirmDlg();
								break main;
							}
							case 199:
							{
								msg = new RequestPreviewItem();
								break main;
							}
							case 200:
							{
								msg = new RequestSSQStatus();
								break main;
							}
							case 201:
							{
								msg = new PetitionVote();
								break main;
							}
							case 202:
							{
								break main;
							}
							case 203:
							{
								msg = new ReplyGameGuardQuery();
								break main;
							}
							case 204:
							{
								msg = new RequestPledgePower();
								break main;
							}
							case 205:
							{
								msg = new RequestMakeMacro();
								break main;
							}
							case 206:
							{
								msg = new RequestDeleteMacro();
								break main;
							}
							case 207:
							{
								msg = new RequestProcureCrop();
								break main;
							}
							case 208:
							{
								int id3 = buf.getShort() & 0xFFFF;
								//								System.out.println(id3);
								switch(id3)
								{
									case 0:
									{
										break main;
									}
									case 1:
									{
										msg = new RequestManorList();
										break main;
									}
									case 2:
									{
										msg = new RequestProcureCropList();
										break main;
									}
									case 3:
									{
										msg = new RequestSetSeed();
										break main;
									}
									case 4:
									{
										msg = new RequestSetCrop();
										break main;
									}
									case 5:
									{
										msg = new RequestWriteHeroWords();
										break main;
									}
									case 6:
									{
										msg = new RequestExMPCCAskJoin();
										break main;
									}
									case 7:
									{
										msg = new RequestExMPCCAcceptJoin();
										break main;
									}
									case 8:
									{
										msg = new RequestExOustFromMPCC();
										break main;
									}
									case 9:
									{
										msg = new RequestOustFromPartyRoom();
										break main;
									}
									case 10:
									{
										msg = new RequestDismissPartyRoom();
										break main;
									}
									case 11:
									{
										msg = new RequestWithdrawPartyRoom();
										break main;
									}
									case 12:
									{
										msg = new RequestHandOverPartyMaster();
										break main;
									}
									case 13:
									{
										msg = new RequestAutoSoulShot();
										break main;
									}
									case 14:
									{
										break main;
									}
									case 15:
									{
										int type = buf.getInt();
										switch(type)
										{
											case 0:
											{
												break main;
											}
											case 1:
											{
												break main;
											}
											case 2:
											{
												break main;
											}
											case 3:
											{
												break main;
											}
											case 4:
											{
												break main;
											}
											default:
											{
												client.onUnknownPacket();
												_log.warn("Unknown client packet! State: IN_GAME, packet ID: " + Integer.toHexString(id).toUpperCase() + ":" + Integer.toHexString(id3).toUpperCase() + ":" + Integer.toHexString(type).toUpperCase());
												break main;
											}
										}
									}
									case 16:
									{
										msg = new RequestPledgeCrestLarge();
										break main;
									}
									case 17:
									{
										msg = new RequestExSetPledgeCrestLargeFirstPart();
										break main;
									}
									case 18:
									{
										msg = new RequestPledgeSetAcademyMaster();
										break main;
									}
									case 19:
									{
										msg = new RequestPledgePowerGradeList();
										break main;
									}
									case 20:
									{
										msg = new RequestPledgeMemberPowerInfo();
										break main;
									}
									case 21:
									{
										msg = new RequestPledgeSetMemberPowerGrade();
										break main;
									}
									case 22:
									{
										msg = new RequestPledgeMemberInfo();
										break main;
									}
									case 23:
									{
										msg = new RequestPledgeWarList();
										break main;
									}
									case 24:
									{
										break main;
									}
									case 0x19:
									{
										msg = new RequestPCCafeCouponUse();
										break main;
									}
									case 26:
									{
										break main;
									}
									case 27:
									{
										msg = new RequestDuelStart();
										break main;
									}
									case 28:
									{
										msg = new RequestDuelAnswerStart();
										break main;
									}
									case 29:
									{
										msg = new RequestTutorialClientEvent();
										break main;
									}
									case 30:
									{
										msg = new RequestExRqItemLink();
										break main;
									}
									case 31:
									{
										msg = new CannotMoveAnymoreInVehicle();
										break main;
									}
									case 32:
									{
										break main;
									}
									case 33:
									{
										msg = new RequestKeyMapping();
										break main;
									}
									case 34:
									{
										msg = new RequestSaveKeyMapping();
										break main;
									}
									case 35:
									{
										msg = new RequestExRemoveItemAttribute();
										break main;
									}
									case 36:
									{
										msg = new RequestSaveInventoryOrder();
										break main;
									}
									case 37:
									{
										msg = new RequestExitPartyMatchingWaitingRoom();
										break main;
									}
									case 0x26:
										msg = new RequestConfirmTargetItem();
										break main;
									case 0x27:
										msg = new RequestConfirmRefinerItem();
										break main;
									case 0x28:
										msg = new RequestConfirmGemStone();
										break main;
									case 0x29:
									{
										msg = new RequestOlympiadObserverEnd();
										break main;
									}
									case 0x2a:
									{
										msg = new RequestCursedWeaponList();
										break main;
									}
									case 0x2b:
									{
										msg = new RequestCursedWeaponLocation();
										break main;
									}
									case 44:
									{
										msg = new RequestPledgeReorganizeMember();
										break main;
									}
									case 45:
									{
										msg = new RequestExMPCCShowPartyMembersInfo();
										break main;
									}
									case 46:
									{
										break main;
									}
									case 47:
									{
										msg = new RequestAskJoinPartyRoom();
										break main;
									}
									case 48:
									{
										msg = new AnswerJoinPartyRoom();
										break main;
									}
									case 49:
									{
										msg = new RequestListPartyMatchingWaitingRoom();
										break main;
									}
									case 50:
									{
										break main;
									}
									case 51:
									{
										break main;
									}
									case 53:
									{
										break main;
									}
									case 54:
									{
										break main;
									}
									case 55:
									{
										break main;
									}
									case 56:
									{
										msg = new RequestExChangeName();
										break main;
									}
									case 57:
									{
										msg = new RequestAllCastleInfo();
										break main;
									}
									case 58:
									{
										break main;
									}
									case 59:
									{
										msg = new RequestAllAgitInfo();
										break main;
									}
									case 60:
									{
										break main;
									}
									case 0x3d:
									{
										break main;
									}
									case 0x3e:
										msg = new RequestRefine();
										break main;
									case 0x3f:
										msg = new RequestConfirmCancelItem();
										break main;
									case 0x40:
										msg = new RequestRefineCancel();
										break main;
									case 65:
									{
										msg = new RequestExMagicSkillUseGround();
										break main;
									}
									case 66:
									{
										msg = new RequestDuelSurrender();
										break main;
									}
									case 67:
									{
										break main;
									}
									case 69:
									{
										break main;
									}
									case 70:
									{
										msg = new RequestPVPMatchRecord();
										break main;
									}
									case 71:
									{
										msg = new SetPrivateStoreWholeMsg();
										break main;
									}
									case 72:
									{
										msg = new RequestDispel();
										break main;
									}
									case 73:
									{
										msg = new RequestExTryToPutEnchantTargetItem();
										break main;
									}
									case 74:
									{
										msg = new RequestExTryToPutEnchantSupportItem();
										break main;
									}
									case 75:
									{
										msg = new RequestExCancelEnchantItem();
										break main;
									}
									case 76:
									{
										msg = new RequestChangeNicknameColor();
										break main;
									}
									case 77:
									{
										msg = new RequestResetNickname();
										break main;
									}
									case 78:
									{
										int id4 = buf.getInt();
										switch(id4)
										{
											case 0:
											{
												msg = new RequestBookMarkSlotInfo();
												break main;
											}
											case 1:
											{
												msg = new RequestSaveBookMarkSlot();
												break main;
											}
											case 2:
											{
												msg = new RequestModifyBookMarkSlot();
												break main;
											}
											case 3:
											{
												msg = new RequestDeleteBookMarkSlot();
												break main;
											}
											case 4:
											{
												msg = new RequestTeleportBookMark();
												break main;
											}
											case 5:
											{
												msg = new RequestChangeBookMarkSlot();
												break main;
											}
											default:
											{
												client.onUnknownPacket();
												_log.warn("Unknown client packet! State: IN_GAME, packet ID: " + Integer.toHexString(id).toUpperCase() + ":" + Integer.toHexString(id3).toUpperCase() + ":" + Integer.toHexString(id4).toUpperCase());
												break main;
											}
										}
									}
									case 79:
									{
										break main;
									}
									case 80:
									{
										msg = new RequestExJump();
										break main;
									}
									case 81:
									{
										break main;
									}
									case 82:
									{
										break main;
									}
									case 83:
									{
										msg = new NotifyStartMiniGame();
										break main;
									}
									case 84:
									{
										msg = new RequestExJoinDominionWar();
										break main;
									}
									case 85:
									{
										msg = new RequestExDominionInfo();
										break main;
									}
									case 86:
									{
										msg = new RequestExCleftEnter();
										break main;
									}
									case 87:
									{
										break main;
									}
									case 88:
									{
										msg = new RequestExEndScenePlayer();
										break main;
									}
									case 89:
									{
										break main;
									}
									case 90:
									{
										msg = new RequestExListMpccWaiting();
										break main;
									}
									case 91:
									{
										msg = new RequestExManageMpccRoom();
										break main;
									}
									case 92:
									{
										msg = new RequestExJoinMpccRoom();
										break main;
									}
									case 93:
									{
										msg = new RequestExOustFromMpccRoom();
										break main;
									}
									case 94:
									{
										msg = new RequestExDismissMpccRoom();
										break main;
									}
									case 95:
									{
										msg = new RequestExWithdrawMpccRoom();
										break main;
									}
									case 96:
									{
										break main;
									}
									case 97:
									{
										msg = new RequestExMpccPartymasterList();
										break main;
									}
									case 98:
									{
										msg = new RequestExPostItemList();
										break main;
									}
									case 99:
									{
										msg = new RequestExSendPost();
										break main;
									}
									case 100:
									{
										msg = new RequestExRequestReceivedPostList();
										break main;
									}
									case 101:
									{
										msg = new RequestExDeleteReceivedPost();
										break main;
									}
									case 102:
									{
										msg = new RequestExRequestReceivedPost();
										break main;
									}
									case 103:
									{
										msg = new RequestExReceivePost();
										break main;
									}
									case 104:
									{
										msg = new RequestExRejectPost();
										break main;
									}
									case 105:
									{
										msg = new RequestExRequestSentPostList();
										break main;
									}
									case 106:
									{
										msg = new RequestExDeleteSentPost();
										break main;
									}
									case 107:
									{
										msg = new RequestExRequestSentPost();
										break main;
									}
									case 108:
									{
										msg = new RequestExCancelSentPost();
										break main;
									}
									case 109:
									{
										msg = new RequestExShowNewUserPetition();
										break main;
									}
									case 110:
									{
										msg = new RequestExShowStepTwo();
										break main;
									}
									case 111:
									{
										msg = new RequestExShowStepThree();
										break main;
									}
									case 112:
									{
										break main;
									}
									case 113:
									{
										break main;
									}
									case 114:
									{
										msg = new RequestExRefundItem();
										break main;
									}
									case 115:
									{
										msg = new RequestExBuySellUIClose();
										break main;
									}
									case 116:
									{
										msg = new RequestExEventMatchObserverEnd();
										break main;
									}
									case 117:
									{
										msg = new RequestPartyLootModification();
										break main;
									}
									case 118:
									{
										msg = new AnswerPartyLootModification();
										break main;
									}
									case 119:
									{
										msg = new AnswerCoupleAction();
										break main;
									}
									case 120:
									{
										msg = new RequestExBR_EventRankerList();
										break main;
									}
									case 121:
									{
										break main;
									}
									case 122:
									{
										msg = new RequestAddExpandQuestAlarm();
										break main;
									}
									case 123:
									{
										msg = new RequestVoteNew();
										break main;
									}
									case 124:
									{
										msg = new RequestGetOnShuttle();
										break main;
									}
									case 125:
									{
										msg = new RequestGetOffShuttle();
										break main;
									}
									case 126:
									{
										msg = new RequestMoveToLocationInShuttle();
										break main;
									}
									case 127:
									{
										msg = new CannotMoveAnymoreInVehicle();
										break main;
									}
									case 128:
									{
										int id5 = buf.getInt();
										switch(id5)
										{
											case 1:
											{
												break main;
											}
											case 2:
											{
												break main;
											}
											case 3:
											{
												break main;
											}
											case 4:
											{
												break main;
											}
											case 5:
											{
												break main;
											}
											case 7:
											{
												break main;
											}
											case 8:
											{
												break main;
											}
											case 9:
											{
												break main;
											}
											case 16:
											{
												break main;
											}
											case 17:
											{
												break main;
											}
											case 18:
											{
												break main;
											}
											case 19:
											{
												break main;
											}
											case 20:
											{
												break main;
											}
											case 13:
											{
												break main;
											}
											case 14:
											{
												break main;
											}
											case 15:
											{
												break main;
											}
											case 10:
											{
												break main;
											}
											default:
											{
												client.onUnknownPacket();
												_log.warn("Unknown client packet! State: IN_GAME, packet ID: " + Integer.toHexString(id).toUpperCase() + ":" + Integer.toHexString(id3).toUpperCase() + ":" + Integer.toHexString(id5).toUpperCase());
												break main;
											}
										}
									}
									case 129:
									{
										msg = new RequestExAddPostFriendForPostBox();
										break main;
									}
									case 130:
									{
										msg = new RequestExDeletePostFriendForPostBox();
										break main;
									}
									case 131:
									{
										msg = new RequestExShowPostFriendListForPostBox();
										break main;
									}
									case 132:
									{
										msg = new RequestExFriendListForPostBox();
										break main;
									}
									case 133:
									{
										msg = new RequestOlympiadMatchList();
										break main;
									}
									case 134:
									{
										msg = new RequestExBR_GamePoint();
										break main;
									}
									case 135:
									{
										msg = new RequestExBR_ProductList();
										break main;
									}
									case 136:
									{
										msg = new RequestExBR_ProductInfo();
										break main;
									}
									case 137:
									{
										msg = new RequestExBR_BuyProduct();
										break main;
									}
									case 138:
									{
										msg = new RequestExBR_RecentProductList();
										break main;
									}
									case 139:
									{
										msg = new RequestBR_MiniGameLoadScores();
										break main;
									}
									case 140:
									{
										msg = new RequestBR_MiniGameInsertScore();
										break main;
									}
									case 141:
									{
										msg = new RequestExBR_LectureMark();
										break main;
									}
									case 142:
									{
										msg = new RequestCrystallizeEstimate();
										break main;
									}
									case 143:
									{
										msg = new RequestCrystallizeItemCancel();
										break main;
									}
									case 144:
									{
										msg = new RequestExEscapeScene();
										break main;
									}
									case 145:
									{
										break main;
									}
									case 146:
									{
										break main;
									}
									case 147:
									{
										int id6 = buf.get();
										switch(id6)
										{
											case 2:
											{
												break main;
											}
											case 3:
											{
												break main;
											}
											case 4:
											{
												break main;
											}
											default:
											{
												client.onUnknownPacket();
												_log.warn("Unknown client packet! State: IN_GAME, packet ID: " + Integer.toHexString(id).toUpperCase() + ":" + Integer.toHexString(id3).toUpperCase() + ":" + Integer.toHexString(id6).toUpperCase());
												break main;
											}
										}
									}
									case 148:
									{
										msg = new RequestFriendDetailInfo();
										break main;
									}
									case 149:
									{
										msg = new RequestUpdateFriendMemo();
										break main;
									}
									case 150:
									{
										msg = new RequestUpdateBlockMemo();
										break main;
									}
									case 151:
									{
										break main;
									}
									case 152:
									{
										break main;
									}
									case 153:
									{
										break main;
									}
									case 154:
									{
										break main;
									}
									case 155:
									{
										break main;
									}
									case 156:
									{
										break main;
									}
									case 157:
									{
										break main;
									}
									case 158:
									{
										break main;
									}
									case 159:
									{
										break main;
									}
									case 160:
									{
										break main;
									}
									case 161:
									{
										break main;
									}
									case 162:
									{
										break main;
									}
									case 163:
									{
										break main;
									}
									case 164:
									{
										break main;
									}
									case 165:
									{
										msg = new RequestRegistPartySubstitute();
										break main;
									}
									case 166:
									{
										msg = new RequestDeletePartySubstitute();
										break main;
									}
									case 167:
									{
										msg = new RequestRegistWaitingSubstitute();
										break main;
									}
									case 168:
									{
										msg = new RequestAcceptWaitingSubstitute();
										break main;
									}
									case 169:
									{
										break main;
									}
									case 170:
									{
										msg = new RequestGoodsInventoryInfo();
										break main;
									}
									case 171:
									{
										int id7 = buf.getInt();
										switch(id7)
										{
											case 0:
											{
												break main;
											}
											case 1:
											{
												break main;
											}
											default:
											{
												client.onUnknownPacket();
												_log.warn("Unknown client packet! State: IN_GAME, packet ID: " + Integer.toHexString(id).toUpperCase() + ":" + Integer.toHexString(id7).toUpperCase());
												break main;
											}
										}
									}
									case 172:
									{
										msg = new RequestFirstPlayStart();
										break main;
									}
									case 173:
									{
										break main;
									}
									case 174:
									{
										msg = new RequestHardWareInfo();
										break main;
									}
									case 176:
									{
										msg = new SendChangeAttributeTargetItem();
										break main;
									}
									case 177:
									{
										msg = new RequestChangeAttributeItem();
										break main;
									}
									case 178:
									{
										msg = new RequestChangeAttributeCancel();
										break main;
									}
									case 179:
									{
										msg = new RequestBR_PresentBuyProduct();
										break main;
									}
									case 180:
									{
										break main;
									}
									case 181:
									{
										break main;
									}
									case 182:
									{
										break main;
									}
									case 183:
									{
										break main;
									}
									case 184:
									{
										break main;
									}
									case 185:
									{
										msg = new RequestJoinPledgeByName();
										break main;
									}
									case 186:
									{
										msg = new RequestInzoneWaitingTime();
										break main;
									}
									case 187:
									{
										break main;
									}
									case 188:
									{
										break main;
									}
									case 189:
									{
										break main;
									}
									case 190:
									{
										break main;
									}
									case 191:
									{
										break main;
									}
									case 192:
									{
										break main;
									}
									case 193:
									{
										break main;
									}
									case 194:
									{
										break main;
									}
									case 195:
									{
										break main;
									}
									case 196:
									{
										break main;
									}
									case 197:
									{
										break main;
									}
									case 198:
									{
										break main;
									}
									case 199:
									{
										break main;
									}
									case 200:
									{
										break main;
									}
									case 201:
									{
										break main;
									}
									case 202:
									{
										break main;
									}
									case 203:
									{
										break main;
									}
									case 204:
									{
										break main;
									}
									case 205:
									{
										break main;
									}
									case 206:
									{
										break main;
									}
									case 0xcf:
									{
										msg = RequestBR_AddBasketProductInfo.INSTANCE;
										break main;
									}
									case 0xd0:
									{
										msg = RequestBR_DeleteBasketProductInfo.INSTANCE;
										break main;
									}
									case 0xd1:
									{
										msg = new RequestBR_NewIConCashBtnWnd();
										break main;
									}
									case 210:
									{
										break main;
									}
									case 211:
									{
										msg = new RequestPledgeRecruitInfo();
										break main;
									}
									case 212:
									{
										msg = new RequestPledgeRecruitBoardSearch();
										break main;
									}
									case 213:
									{
										msg = new RequestPledgeRecruitBoardAccess();
										break main;
									}
									case 214:
									{
										msg = new RequestPledgeRecruitBoardDetail();
										break main;
									}
									case 215:
									{
										msg = new RequestPledgeWaitingApply();
										break main;
									}
									case 216:
									{
										msg = new RequestPledgeWaitingApplied();
										break main;
									}
									case 217:
									{
										msg = new RequestPledgeWaitingList();
										break main;
									}
									case 218:
									{
										msg = new RequestPledgeWaitingUser();
										break main;
									}
									case 219:
									{
										msg = new RequestPledgeWaitingUserAccept();
										break main;
									}
									case 220:
									{
										msg = new RequestPledgeDraftListSearch();
										break main;
									}
									case 221:
									{
										msg = new RequestPledgeDraftListApply();
										break main;
									}
									case 222:
									{
										msg = new RequestPledgeRecruitApplyInfo();
										break main;
									}
									case 223:
									{
										msg = new RequestPledgeJoinSys();
										break main;
									}
									case 224:
									{
										break main;
									}
									case 225:
									{
										msg = new NotifyExitBeautyshop();
										break main;
									}
									case 226:
									{
										break main;
									}
									case 227:
									{
										msg = new RequestExAddEnchantScrollItem();
										break main;
									}
									case 228:
									{
										msg = new RequestExRemoveEnchantSupportItem();
										break main;
									}
									case 229:
									{
										break main;
									}
									case 230:
									{
										msg = new RequestDivideAdenaStart();
										break main;
									}
									case 231:
									{
										msg = new RequestDivideAdenaCancel();
										break main;
									}
									case 232:
									{
										msg = new RequestDivideAdena();
										break main;
									}
									case 233:
									{
										break main;
									}
									case 234:
									{
										break main;
									}
									case 235:
									{
										break main;
									}
									case 236:
									{
										break main;
									}
									case 237:
									{
										msg = new RequestStopMove();
										break main;
									}
									case 238:
									{
										break main;
									}
									case 239:
									{
										break main;
									}
									case 240:
									{
										msg = new ExPCCafeRequestOpenWindowWithoutNPC();
										break main;
									}
									case 242:
									{
										break main;
									}
									case 243:
									{
										break main;
									}
									case 244:
									{
										msg = new RequestNewEnchantPushOne();
										break main;
									}
									case 245:
									{
										msg = new RequestNewEnchantRemoveOne();
										break main;
									}
									case 246:
									{
										msg = new RequestNewEnchantPushTwo();
										break main;
									}
									case 247:
									{
										msg = new RequestNewEnchantRemoveTwo();
										break main;
									}
									case 248:
									{
										msg = new RequestNewEnchantClose();
										break main;
									}
									case 249:
									{
										msg = new RequestNewEnchantTry();
										break main;
									}
									case 250:
									{
										msg = new RequestNewEnchantRetryToPutItems();
										break main;
									}
									case 251:
									{
										break main;
									}
									case 252:
									{
										break main;
									}
									case 253:
									{
										break main;
									}
									case 254:
									{
										msg = new ExSendSelectedQuestZoneID();
										break main;
									}
									case 255:
									{
										break main;
									}
									case 256:
									{
										break main;
									}
									case 257:
									{
										break main;
									}
									case 258:
									{
										break main;
									}
									case 259:
									{
										break main;
									}
									case 260:
									{
										break main;
									}
									case 261:
									{
										msg = new RequestExAutoFish();
										break main;
									}
									case 262:
									{
										msg = new RequestVipAttendanceItemList();
										break main;
									}
									case 263:
									{
										msg = new RequestVipAttendanceCheck();
										break main;
									}
									case 264:
									{
										msg = new RequestItemEnsoul();
										break main;
									}
									case 265:
									{
										break main;
									}
									case 266:
									{
										break main;
									}
									case 267:
									{
										break main;
									}
									case 268:
									{
										break main;
									}
									case 269:
									{
										break main;
									}
									case 270:
									{
										break main;
									}
									case 271:
									{
										msg = new RequestTodoList();
										break main;
									}
									case 272:
									{
										msg = new RequestTodoListHTML();
										break main;
									}
									case 273:
									{
										msg = new Unk0();
										break main;
									}
									case 274:
									{
										msg = new RequestPledgeBonusOpen();
										break main;
									}
									case 275:
									{
										break main;
									}
									case 276:
									{
										break main;
									}
									case 277:
									{
										break main;
									}
									case 285:
									{
										msg = new RequestTodoList();
										break main;
									}
									case 286:
									{
										msg = new RequestTodoListHTML();
										break main;
									}
									case 287:
									{
										msg = new RequestOneDayRewardReceive();
										break main;
									}
									case 289:
									{
										msg = new RequestPledgeBonusOpen();
										break main;
									}
									case 290:
									{
										msg = new RequestPledgeBonusRewardList();
										break main;
									}
									case 291:
									{
										msg = new RequestPledgeBonusReward();
										break main;
									}
									case 0x127:
									{
										msg = new RequestTryEnSoulExtraction();
										break main;
									}
									default:
									{
										client.onUnknownPacket();
										_log.warn("Unknown client packet! State: IN_GAME, packet ID: " + Integer.toHexString(id).toUpperCase() + ":" + Integer.toHexString(id3).toUpperCase());
										break main;
									}
								}
							}
							default:
							{
								client.onUnknownPacket();
								break main;
							}
						}
					}
				}
			}
		}
		catch(BufferUnderflowException e)
		{
			client.onPacketReadFail();
		}
		return msg;
	}

	@Override
	public GameClient create(MMOConnection<GameClient> con)
	{
		return new GameClient(con);
	}

	@Override
	public void execute(Runnable r)
	{
		ThreadPoolManager.getInstance().execute(r);
	}

	static
	{
		_log = LoggerFactory.getLogger(GamePacketHandler.class);
	}
}
