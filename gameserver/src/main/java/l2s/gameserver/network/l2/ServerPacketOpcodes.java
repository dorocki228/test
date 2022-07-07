package l2s.gameserver.network.l2;

public enum ServerPacketOpcodes
{
	DiePacket,
	RevivePacket,
	AttackOutofRangePacket,
	AttackinCoolTimePacket,
	AttackDeadTargetPacket,
	SpawnItemPacket,
	SellListPacket,
	BuyListPacket,
	DeleteObjectPacket,
	CharacterSelectionInfoPacket,
	LoginResultPacket,
	CharacterSelectedPacket,
	NpcInfoPacket,
	NewCharacterSuccessPacket,
	NewCharacterFailPacket,
	CharacterCreateSuccessPacket,
	CharacterCreateFailPacket,
	ItemListPacket,
	SunRisePacket,
	SunSetPacket,
	TradeStartPacket,
	TradeStartOkPacket,
	DropItemPacket,
	GetItemPacket,
	StatusUpdatePacket,
	NpcHtmlMessagePacket,
	TradeOwnAddPacket,
	TradeOtherAddPacket,
	TradeDonePacket,
	CharacterDeleteSuccessPacket,
	CharacterDeleteFailPacket,
	ActionFailPacket,
	SeverClosePacket,
	InventoryUpdatePacket,
	TeleportToLocationPacket,
	TargetSelectedPacket,
	TargetUnselectedPacket,
	AutoAttackStartPacket,
	AutoAttackStopPacket,
	SocialActionPacket,
	ChangeMoveTypePacket,
	ChangeWaitTypePacket,
	ManagePledgePowerPacket,
	CreatePledgePacket,
	AskJoinPledgePacket,
	JoinPledgePacket,
	VersionCheckPacket,
	MTLPacket,
	NSPacket,
	CIPacket,
	UIPacket,
	AttackPacket,
	WithdrawalPledgePacket,
	OustPledgeMemberPacket,
	SetOustPledgeMemberPacket,
	DismissPledgePacket,
	SetDismissPledgePacket,
	AskJoinPartyPacket,
	JoinPartyPacket,
	WithdrawalPartyPacket,
	OustPartyMemberPacket,
	SetOustPartyMemberPacket,
	DismissPartyPacket,
	SetDismissPartyPacket,
	MagicAndSkillList,
	WareHouseDepositListPacket,
	WareHouseWithdrawListPacket,
	WareHouseDonePacket,
	ShortCutRegisterPacket,
	ShortCutInitPacket,
	ShortCutDeletePacket,
	StopMovePacket,
	MagicSkillUse,
	MagicSkillCanceled,
	SayPacket2,
	NpcInfoAbnormalVisualEffect,
	DoorInfoPacket,
	DoorStatusUpdatePacket,
	PartySmallWindowAllPacket,
	PartySmallWindowAddPacket,
	PartySmallWindowDeleteAllPacket,
	PartySmallWindowDeletePacket,
	PartySmallWindowUpdatePacket,
	TradePressOwnOkPacket,
	MagicSkillLaunchedPacket,
	FriendAddRequestResult,
	FriendAdd,
	FriendRemove,
	FriendList,
	FriendStatus,
	PledgeShowMemberListAllPacket,
	PledgeShowMemberListUpdatePacket,
	PledgeShowMemberListAddPacket,
	PledgeShowMemberListDeletePacket,
	MagicListPacket,
	SkillListPacket,
	VehicleInfoPacket,
	FinishRotatingPacket,
	SystemMessagePacket,
	StartPledgeWarPacket,
	ReplyStartPledgeWarPacket,
	StopPledgeWarPacket,
	ReplyStopPledgeWarPacket,
	SurrenderPledgeWarPacket,
	ReplySurrenderPledgeWarPacket,
	SetPledgeCrestPacket,
	PledgeCrestPacket,
	SetupGaugePacket,
	VehicleDeparturePacket,
	VehicleCheckLocationPacket,
	GetOnVehiclePacket,
	GetOffVehiclePacket,
	TradeRequestPacket,
	RestartResponsePacket,
	MTPPacket,
	SSQInfoPacket,
	GameGuardQueryPacket,
	L2FriendListPacket,
	L2FriendPacket,
	L2FriendStatusPacket,
	L2FriendSayPacket,
	ValidateLocationPacket,
	StartRotatingPacket,
	ShowBoardPacket,
	ChooseInventoryItemPacket,
	DummyPacket,
	MoveToLocationInVehiclePacket,
	StopMoveInVehiclePacket,
	ValidateLocationInVehiclePacket,
	TradeUpdatePacket,
	TradePressOtherOkPacket,
	FriendAddRequest,
	LogOutOkPacket,
	AbnormalStatusUpdatePacket,
	QuestListPacket,
	EnchantResultPacket,
	PledgeShowMemberListDeleteAllPacket,
	PledgeInfoPacket,
	PledgeExtendedInfoPacket,
	SummonInfoPacket,
	RidePacket,
	DummyPacket1,
	PledgeShowInfoUpdatePacket,
	ClientActionPacket,
	AcquireSkillListPacket,
	AcquireSkillInfoPacket,
	ServerObjectInfoPacket,
	GMHidePacket,
	AcquireSkillDonePacket,
	GMViewCharacterInfoPacket,
	GMViewPledgeInfoPacket,
	GMViewSkillInfoPacket,
	GMViewMagicInfoPacket,
	GMViewQuestInfoPacket,
	GMViewItemListPacket,
	GMViewWarehouseWithdrawListPacket,
	ListPartyWatingPacket,
	PartyRoomInfoPacket,
	PlaySoundPacket,
	StaticObjectPacket,
	PrivateStoreManageList,
	PrivateStoreList,
	PrivateStoreMsg,
	ShowMinimapPacket,
	ReviveRequestPacket,
	AbnormalVisualEffectPacket,
	TutorialShowHtmlPacket,
	TutorialShowQuestionMarkPacket,
	TutorialEnableClientEventPacket,
	TutorialCloseHtmlPacket,
	ShowRadarPacket,
	WithdrawAlliancePacket,
	OustAllianceMemberPledgePacket,
	DismissAlliancePacket,
	SetAllianceCrestPacket,
	AllianceCrestPacket,
	ServerCloseSocketPacket,
	PetStatusShowPacket,
	PetInfoPacket,
	PetItemListPacket,
	PetInventoryUpdatePacket,
	AllianceInfoPacket,
	PetStatusUpdatePacket,
	PetDeletePacket,
	DeleteRadarPacket,
	MyTargetSelectedPacket,
	PartyMemberPositionPacket,
	AskJoinAlliancePacket,
	JoinAlliancePacket,
	PrivateStoreBuyManageList,
	PrivateStoreBuyList,
	PrivateStoreBuyMsg,
	VehicleStartPacket,
	NpcInfoState,
	StartAllianceWarPacket,
	ReplyStartAllianceWarPacket,
	StopAllianceWarPacket,
	ReplyStopAllianceWarPacket,
	SurrenderAllianceWarPacket,
	SkillCoolTimePacket,
	PackageToListPacket,
	CastleSiegeInfoPacket,
	CastleSiegeAttackerListPacket,
	CastleSiegeDefenderListPacket,
	NickNameChangedPacket,
	PledgeStatusChangedPacket,
	RelationChangedPacket,
	EventTriggerPacket,
	MultiSellListPacket,
	SetSummonRemainTimePacket,
	PackageSendableListPacket,
	EarthQuakePacket,
	FlyToLocationPacket,
	BlockListPacket,
	SpecialCameraPacket,
	NormalCameraPacket,
	SkillRemainSecPacket,
	NetPingPacket,
	DicePacket,
	SnoopPacket,
	RecipeBookItemListPacket,
	RecipeItemMakeInfoPacket,
	RecipeShopManageListPacket,
	RecipeShopSellListPacket,
	RecipeShopItemInfoPacket,
	RecipeShopMsgPacket,
	ShowCalcPacket,
	MonRaceInfoPacket,
	HennaItemInfoPacket,
	HennaInfoPacket,
	HennaUnequipListPacket,
	HennaUnequipInfoPacket,
	MacroListPacket,
	BuyListSeedPacket,
	ShowTownMapPacket,
	ObserverStartPacket,
	ObserverEndPacket,
	ChairSitPacket,
	HennaEquipListPacket,
	SellListProcurePacket,
	GMHennaInfoPacket,
	RadarControlPacket,
	ClientSetTimePacket,
	ConfirmDlgPacket,
	PartySpelledPacket,
	ShopPreviewListPacket,
	ShopPreviewInfoPacket,
	CameraModePacket,
	ShowXMasSealPacket,
	EtcStatusUpdatePacket,
	ShortBuffStatusUpdatePacket,
	SSQStatusPacket,
	PetitionVotePacket,
	AgitDecoInfoPacket,
	DummyPacket2,
	ExDummyPacket(0),
	ExRegenMaxPacket(1),
	ExEventMatchUserInfoPacket(2),
	ExColosseumFenceInfoPacket(3),
	ExEventMatchSpelledInfoPacket(4),
	ExEventMatchFirecrackerPacket(5),
	ExEventMatchTeamUnlockedPacket(6),
	ExEventMatchGMTestPacket(7),
	ExPartyRoomMemberPacket(8),
	ExClosePartyRoomPacket(9),
	ExManagePartyRoomMemberPacket(10),
	ExEventMatchLockResult(11),
	ExAutoSoulShot(12),
	ExEventMatchListPacket(13),
	ExEventMatchObserverPacket(14),
	ExEventMatchMessagePacket(15),
	ExEventMatchScorePacket(16),
	ExServerPrimitivePacket(17),
	ExOpenMPCCPacket(18),
	ExCloseMPCCPacket(19),
	ExShowCastleInfo(20),
	ExShowFortressInfo(21),
	ExShowAgitInfo(22),
	ExShowFortressSiegeInfo(23),
	ExPartyPetWindowAdd(24),
	ExPartyPetWindowUpdate(25),
	ExAskJoinMPCCPacket(26),
	ExPledgeEmblem(27),
	ExEventMatchTeamInfoPacket(28),
	ExEventMatchCreatePacket(29),
	ExFishingStartPacket(30),
	ExFishingEndPacket(31),
	ExShowQuestInfoPacket(32),
	ExShowQuestMarkPacket(33),
	ExSendManorListPacket(34),
	ExShowSeedInfoPacket(35),
	ExShowCropInfoPacket(36),
	ExShowManorDefaultInfoPacket(37),
	ExShowSeedSettingPacket(38),
	ExFishingStartCombatPacket(39),
	ExFishingHpRegenPacket(40),
	ExEnchantSkillListPacket(41),
	ExEnchantSkillInfoPacket(42),
	ExShowCropSettingPacket(43),
	ExShowSellCropListPacket(44),
	ExOlympiadMatchEndPacket(45),
	ExMailArrivedPacket(46),
	ExStorageMaxCountPacket(47),
	ExEventMatchManagePacket(48),
	ExMultiPartyCommandChannelInfoPacket(49),
	ExPCCafePointInfoPacket(50),
	ExSetCompassZoneCode(51),
	ExGetBossRecord(52),
	ExAskJoinPartyRoom(53),
	ExListPartyMatchingWaitingRoom(54),
	ExSetMpccRouting(55),
	ExShowAdventurerGuideBook(56),
	ExShowScreenMessage(57),
	PledgeSkillListPacket(58),
	PledgeSkillListAddPacket(59),
	PledgeSkillListRemovePacket(60),
	PledgePowerGradeList(61),
	PledgeReceivePowerInfo(62),
	PledgeReceiveMemberInfo(63),
	PledgeReceiveWarList(64),
	PledgeReceiveSubPledgeCreated(65),
	ExRedSkyPacket(66),
	PledgeReceiveUpdatePower(67),
	FlySelfDestinationPacket(68),
	ShowPCCafeCouponShowUI(0x45),
	ExSearchOrc(70),
	ExCursedWeaponList(71),
	ExCursedWeaponLocation(72),
	ExRestartClient(73),
	ExRequestHackShield(74),
	ExUseSharedGroupItem(75),
	ExMPCCShowPartyMemberInfo(76),
	ExDuelAskStart(77),
	ExDuelReady(78),
	ExDuelStart(79),
	ExDuelEnd(80),
	ExDuelUpdateUserInfo(81),
	ExShowVariationMakeWindow(82),
	ExShowVariationCancelWindow(83),
	ExPutItemResultForVariationMake(84),
	ExPutIntensiveResultForVariationMake(85),
	ExPutCommissionResultForVariationMake(86),
	ExVariationResult(87),
	ExPutItemResultForVariationCancel(88),
	ExVariationCancelResult(89),
	ExDuelEnemyRelation(90),
	ExPlayAnimation(91),
	ExMPCCPartyInfoUpdate(92),
	ExPlayScene(93),
	ExSpawnEmitterPacket(94),
	ExEnchantSkillInfoDetailPacket(95),
	ExBasicActionList(96),
	ExAirShipInfo(97),
	ExAttributeEnchantResultPacket(98),
	ExChooseInventoryAttributeItemPacket(99),
	ExGetOnAirShipPacket(100),
	ExGetOffAirShipPacket(101),
	ExMoveToLocationAirShipPacket(102),
	ExStopMoveAirShipPacket(103),
	ExShowTrace(104),
	ExItemAuctionInfoPacket(105),
	ExNeedToChangeName(106),
	ExPartyPetWindowDelete(107),
	ExTutorialList(108),
	ExRpItemLink(109),
	ExMoveToLocationInAirShipPacket(110),
	ExStopMoveInAirShipPacket(111),
	ExValidateLocationInAirShipPacket(112),
	ExUISettingPacket(113),
	ExMoveToTargetInAirShipPacket(114),
	ExAttackInAirShipPacket(115),
	ExMagicSkillUseInAirShipPacket(116),
	ExShowBaseAttributeCancelWindow(117),
	ExBaseAttributeCancelResult(118),
	ExSubPledgetSkillAdd(119),
	ExResponseFreeServer(120),
	ExShowProcureCropDetailPacket(121),
	ExHeroListPacket(122),
	ExOlympiadUserInfoPacket(123),
	ExOlympiadSpelledInfoPacket(124),
	ExOlympiadModePacket(125),
	ExShowFortressMapInfo(126),
	ExPVPMatchRecord(127),
	ExPVPMatchUserDie(128),
	ExPrivateStoreWholeMsg(129),
	ExPutEnchantTargetItemResult(130),
	ExPutEnchantSupportItemResult(131),
	ExChangeNicknameNColor(132),
	ExGetBookMarkInfoPacket(133),
	ExNotifyPremiumItem(134),
	ExGetPremiumItemListPacket(135),
	ExPeriodicItemList(136),
	ExJumpToLocation(137),
	ExPVPMatchCCRecord(138),
	ExPVPMatchCCMyRecord(139),
	ExPVPMatchCCRetire(140),
	ExShowTerritory(141),
	ExNpcQuestHtmlMessage(142),
	ExSendUIEventPacket(143),
	ExNotifyBirthDay(144),
	ExShowDominionRegistry(145),
	ExReplyRegisterDominion(146),
	ExReplyDominionInfo(147),
	ExShowOwnthingPos(148),
	ExCleftList(149),
	ExCleftState(150),
	ExDominionChannelSet(151),
	ExBlockUpSetList(152),
	ExBlockUpSetState(153),
	ExStartScenePlayer(154),
	ExAirShipTeleportList(155),
	ExMpccRoomInfo(156),
	ExListMpccWaiting(157),
	ExDissmissMpccRoom(158),
	ExManageMpccRoomMember(159),
	ExMpccRoomMember(160),
	ExVitalityPointInfo(161),
	ExShowSeedMapInfo(162),
	ExMpccPartymasterList(163),
	ExDominionWarStart(164),
	ExDominionWarEnd(165),
	ExShowLines(166),
	ExPartyMemberRenamed(167),
	ExEnchantSkillResult(168),
	ExRefundList(169),
	ExNoticePostArrived(170),
	ExShowReceivedPostList(171),
	ExReplyReceivedPost(172),
	ExShowSentPostList(173),
	ExReplySentPost(174),
	ExResponseShowStepOne(175),
	ExResponseShowStepTwo(176),
	ExResponseShowContents(177),
	ExShowPetitionHtml(178),
	ExReplyPostItemList(179),
	ExChangePostState(180),
	ExReplyWritePost(181),
	ExInitializeSeed(182),
	ExRaidReserveResult(183),
	ExBuySellListPacket(184),
	ExCloseRaidSocket(185),
	ExPrivateMarketListPacket(186),
	ExRaidCharacterSelected(187),
	ExAskCoupleAction(188),
	ExBrBroadcastEventState(189),
	ExBR_LoadEventTopRankersPacket(190),
	ExChangeNPCState(191),
	ExAskModifyPartyLooting(192),
	ExSetPartyLooting(193),
	ExRotation(194),
	ExChangeClientEffectInfo(195),
	ExMembershipInfo(196),
	ExReplyHandOverPartyMaster(197),
	ExQuestNpcLogList(198),
	ExQuestItemListPacket(199),
	ExGMViewQuestItemListPacket(200),
	ExResartResponse(201),
	ExVoteSystemInfoPacket(202),
	ExShuttuleInfoPacket(203),
	ExSuttleGetOnPacket(204),
	ExSuttleGetOffPacket(205),
	ExSuttleMovePacket(206),
	ExMTLInSuttlePacket(207),
	ExStopMoveInShuttlePacket(208),
	ExValidateLocationInShuttlePacket(209),
	ExAgitAuctionCmdPacket(210),
	ExConfirmAddingPostFriend(211),
	ExReceiveShowPostFriend(212),
	ExOlympiadMatchResult(213),
	ExOlympiadMatchList(213),
	ExBR_GamePointPacket(214),
	ExBR_ProductListPacket(215),
	ExBR_ProductInfoPacket(216),
	ExBR_BuyProductPacket(217),
	ExBR_PremiumStatePacket(218),
	ExBrExtraUserInfo(219),
	ExBrBuffEventState(220),
	ExBR_RecentProductListPacket(221),
	ExBR_MinigameLoadScoresPacket(222),
	ExBR_AgathionEnergyInfoPacket(223),
	ExShowChannelingEffectPacket(224),
	ExGetCrystalizingEstimation(225),
	ExGetCrystalizingFail(226),
	ExNavitAdventPointInfoPacket(227),
	ExNavitAdventEffectPacket(228),
	ExNavitAdventTimeChangePacket(229),
	ExAbnormalStatusUpdateFromTargetPacket(230),
	ExStopScenePlayerPacket(231),
	ExFlyMove(232),
	ExDynamicQuestPacket(233),
	ExSubjobInfo(234),
	ExChangeMPCost(235),
	ExFriendDetailInfo(236),
	ExBlockAddResult(237),
	ExBlockRemoveResult(238),
	ExBlockDefailInfo(239),
	ExLoadInzonePartyHistory(240),
	ExFriendNotifyNameChange(241),
	ExShowCommission(242),
	ExResponseCommissionItemList(243),
	ExResponseCommissionInfo(244),
	ExResponseCommissionRegister(245),
	ExResponseCommissionDelete(246),
	ExResponseCommissionList(247),
	ExResponseCommissionBuyInfo(248),
	ExResponseCommissionBuyItem(249),
	ExAcquirableSkillListByClass(250),
	ExMagicAttackInfo(251),
	ExAcquireSkillInfo(252),
	ExNewSkillToLearnByLevelUp(253),
	ExCallToChangeClass(254),
	ExChangeToAwakenedClass(255),
	ExTacticalSign(256),
	ExLoadStatWorldRank(257),
	ExLoadStatUser(258),
	ExLoadStatHotLink(259),
	ExGetWebSessionID(260),
	Ex2NDPasswordCheckPacket(261),
	Ex2NDPasswordVerifyPacket(262),
	Ex2NDPasswordAckPacket(263),
	ExFlyMoveBroadcast(264),
	ExShowUsmPacket(265),
	ExShowStatPage(266),
	ExIsCharNameCreatable(267),
	ExGoodsInventoryChangedNotiPacket(268),
	ExGoodsInventoryInfoPacket(269),
	ExGoodsInventoryResultPacket(270),
	ExAlterSkillRequest(271),
	ExNotifyFlyMoveStart(272),
	ExDummyPacket1(273),
	ExCloseCommission(274),
	ExChangeAttributeItemList(275),
	ExChangeAttributeInfo(276),
	ExChangeAttributeOk(277),
	ExChangeAttributeFail(278),
	ExLightingCandleEvent(279),
	ExVitalityEffectInfo(280),
	ExLoginVitalityEffectInfo(281),
	ExBR_PresentBuyProductPacket(282),
	ExMentorList(283),
	ExMentorAdd(284),
	ListMenteeWaitingPacket(285),
	ExInzoneWaitingInfo(286),
	ExCuriousHouseState(287),
	ExCuriousHouseEnter(288),
	ExCuriousHouseLeave(289),
	ExCuriousHouseMemberList(290),
	ExCuriousHouseMemberUpdate(291),
	ExCuriousHouseRemainTime(292),
	ExCuriousHouseResult(293),
	ExCuriousHouseObserveList(294),
	ExCuriousHouseObserveMode(295),
	ExSysstring(296),
	ExChoose_Shape_Shifting_Item(297),
	ExPut_Shape_Shifting_Target_Item_Result(298),
	ExPut_Shape_Shifting_Extraction_Item_Result(299),
	ExShape_Shifting_Result(300),
	ExCastleState(301),
	ExNCGuardReceiveDataFromServer(302),
	ExKalieEvent(303),
	ExKalieEventJackpotUser(304),
	ExAbnormalVisualEffectInfo(305),
	ExNpcInfoSpeed(306),
	ExSetPledgeEmblemAck(307),
	ExShowBeautyMenuPacket(308),
	ExResponseBeautyListPacket(309),
	ExResponseBeautyRegistResetPacket(310),
	ExResponseResetListPacket(311),
	ExShuffleSeedAndPublicKey(312),
	ExCheck_SpeedHack(313),
	ExBR_NewIConCashBtnWnd(314),
	ExEvent_Campaign_Info(315),
	ExUnReadMailCount(316),
	ExPledgeCount(317),
	ExAdenaInvenCount(318),
	ExPledgeRecruitInfo(319),
	ExPledgeRecruitApplyInfo(320),
	ExPledgeRecruitBoardSearch(321),
	ExPledgeRecruitBoardDetail(322),
	ExPledgeWaitingListApplied(323),
	ExPledgeWaitingList(324),
	ExPledgeWaitingUser(325),
	ExPledgeDraftListSearch(326),
	ExPledgeWaitingListAlarm(327),
	ExValidateActiveCharacter(328),
	ExCloseCommissionRegister(329),
	ExTeleportToLocationActivate(330),
	ExNotifyWebPetitionReplyAlarm(331),
	ExEventShowXMasWishCard(332),
	ExInvitation_Event_UI_Setting(333),
	ExInvitation_Event_Ink_Energy(334),
	Ex_Check_Abusing(335),
	ExGMVitalityEffectInfo(336),
	ExPathToAwakeningAlarm(337),
	ExPutEnchantScrollItemResult(338),
	ExRemoveEnchantSupportItemResult(339),
	ExShowCardRewardList(340),
	ExGmViewCharacterInfo(341),
	ExUserInfoEquipSlot(342),
	ExUserInfoCubic(343),
	ExUserInfoAbnormalVisualEffect(344),
	ExUserInfoFishing(345),
	ExPartySpelledInfoUpdate(346),
	ExDivideAdenaStart(347),
	ExDivideAdenaCancel(348),
	ExDivideAdenaDone(349),
	ExPetInfo(350),
	ExAcquireAPSkillList(351),
	ExStartLuckyGame(352),
	ExBettingLuckyGameResult(353),
	ExTrainingZone_Admission(354),
	ExTrainingZone_Leaving(355),
	ExPeriodicHenna(356),
	ExShowAPListWnd(357),
	ExUserInfoInvenWeight(358),
	ExCloseAPListWnd(359),
	ExEnchantOneOK(360),
	ExEnchantOneFail(361),
	ExEnchantOneRemoveOK(362),
	ExEnchantOneRemoveFail(363),
	ExEnchantTwoOK(364),
	ExEnchantTwoFail(365),
	ExEnchantTwoRemoveOK(366),
	ExEnchantTwoRemoveFail(367),
	ExEnchantSucess(368),
	ExEnchantFail(369),
	ExAccountAttendanceInfo(370),
	ExWorldChatCnt(371),
	ExAlchemySkillList(372),
	ExTryMixCube(373),
	ExAlchemyConversion(374),
	ExBeautyItemList(375),
	ExReceiveClientINI(376),
	ExAutoFishAvailable(379),
	ExChannlChatEnterWorld(378),
	ExChannlChatPlegeInfo(379),
	ExVipAttendanceItemList(380),
	ExConfirmVipAttendanceCheck(381),
	ReciveVipProductList(382),
	ReciveVipLuckyGameInfo(383),
	ExShowEnsoulWindow(384),
	ExEnsoulResult(385),
	ReciveVipInfoRemainTime(386),
	ReceiveVipBotCaptchaImage(387),
	ReceiveVipBotCaptchaAnswerResult(388),
	ExOneDayReceiveRewardList(423),
	ExConnectedTimeAndGettableReward(424),
	ExTodoListRecommend(425),
	ExTodoListInzone(426),
	ExTodoListHTML(427),
	ExPledgeBonusOpen(429),
	ExPledgeBonusList(430),
	ExPledgeBonusMarkReset(431),
	ExPledgeBonusUpdate(432),
	ExEnSoulExtractionShow(0x1B3),
	ExEnSoulExtractionResult(0x1B4);

	public static final ServerPacketOpcodes[] VALUES = values();
	private final int _exOrdinal;

	ServerPacketOpcodes(int exOrdinal)
	{
		_exOrdinal = exOrdinal;
	}

	ServerPacketOpcodes()
	{
		this(0xFF);
	}

	public int getId()
	{
		int ordinal = ordinal();
		if(ordinal >= 254)
			return 254;
		return ordinal;
	}

	public int getExId()
	{
		return _exOrdinal;
	}

}