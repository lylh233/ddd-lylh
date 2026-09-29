import {
    ACTION_TIMEOUT_MS,
    KNIFE_SETTLE_MS,
    ActionResult,
    DAWN_WAIT_MS,
    DEAD_SKIP_MS,
    DeadReason,
    GameAction,
    GameView,
    LogAudience,
    LogEntry,
    PickerState,
    Phase,
    RoleId,
    URGENT_MS,
    campOf,
    isWolfRole,
    roleInfo,
} from './types';

interface PlayerState {
    seat: number;
    name: string;
    role: RoleId;
    alive: boolean;
    deadReason: DeadReason;
    isSheriff: boolean;
    campaigning: boolean;
    revealedIdiot: boolean;
    shotUsed: boolean;
}

type DeathCause = 'knife' | 'poison' | 'exile' | 'shot' | 'explode';

interface PendingDeath {
    seat: number;
    reason: 'killed' | 'exiled';
    cause: DeathCause;
}

type AfterQueue = 'day' | 'night' | null;

const DAY_EXPLODE: Phase[] = [
    'dawn_announce',
    'day_campaign',
    'day_campaign_speech',
    'day_sheriff_vote',
    'day_talk_ready',
    'day_talk',
    'day_exile_vote',
];

export class GameEngine {
    now = 0;
    day = 1;
    phase: Phase = 'night_guard';
    prompt = '';
    winner: 'good' | 'wolf' | null = null;
    players: PlayerState[] = [];
    logs: LogEntry[] = [];
    rev = 0;

    private wolfCount = 0;
    private villagerCount = 0;
    private godCount = 0;
    private deadline: number | null = null;
    private logSeq = 1;
    private noticeSeq = 1;
    private notices: { [seat: number]: { id: number; text: string } | null } = {};

    private guardChoices: { [seat: number]: number | null } = {};
    private lastGuard: { [seat: number]: number | null } = {};
    private wolfChoices: { [seat: number]: number | null | undefined } = {};
    private wolfLocked = false;
    private wolfSettleAt = 0;
    private campaignLogId = 0;
    private knifeTarget: number | null = null;
    private poisonUsed = false;
    private antidoteUsed = false;
    private witchSaved = false;
    private poisonTarget: number | null = null;
    private seerPending: number[] = [];
    private witchPending: number[] = [];

    private queue: PendingDeath[] = [];
    private current: PendingDeath | null = null;
    private afterQueue: AfterQueue = null;
    private badgeOnly = false;
    private waitingDay = false;
    private dawnUnlockAt = 0;
    private dawnPrompt = '';
    private sheriffDone = false;
    private diedThisDay = false;
    private carriedDeaths: number[] = [];

    private voters: number[] = [];
    private votes: { [seat: number]: number | null | undefined } = {};
    private voteCandidates: number[] = [];
    private voteKind: 'sheriff' | 'exile' = 'sheriff';
    private allowAbstain = false;
    private idiotSeat = 0;
    private voteBoard: { seat: number; target: number | null }[] = [];
    private voteText = '';

    start(names: string[], roles: RoleId[]): void {
        if (names.length !== roles.length || names.length === 0) {
            throw new Error('人数与身份数量不一致');
        }
        this.players = names.map((name, index) => ({
            seat: index + 1,
            name: name || `${index + 1}号`,
            role: roles[index],
            alive: true,
            deadReason: 'none',
            isSheriff: false,
            campaigning: false,
            revealedIdiot: false,
            shotUsed: false,
        }));
        this.wolfCount = roles.filter((role) => campOf(role) === 'wolf').length;
        this.villagerCount = roles.filter((role) => campOf(role) === 'villager').length;
        this.godCount = roles.filter((role) => campOf(role) === 'god').length;
        this.day = 1;
        this.winner = null;
        this.logs = [];
        this.sheriffDone = false;
        this.carriedDeaths = [];
        this.players.forEach((player) => {
            this.addLog('self', `你本局的身份是${roleInfo(player.role).name}`, player.seat);
        });
        this.beginNight();
        this.touch();
    }

    tick(ms: number): void {
        if (ms < 0 || this.winner) {
            return;
        }
        this.now += ms;
        if (this.deadline != null && this.now >= this.deadline) {
            this.deadline = null;
            this.onTimeout();
            this.touch();
        }
    }

    act(seat: number, action: GameAction): ActionResult {
        const result = this.apply(seat, action);
        if (result.ok) {
            this.touch();
        }
        return result;
    }

    signature(): string {
        return `${this.rev}:${this.isUrgent() ? 1 : 0}:${this.knifeConfirmReady() ? 1 : 0}`;
    }

    isUrgent(): boolean {
        return this.remainingMs() != null && this.remainingMs()! <= URGENT_MS;
    }

    remainingMs(): number | null {
        if (this.deadline == null) {
            return null;
        }
        return Math.max(0, this.deadline - this.now);
    }

    view(seat: number): GameView {
        const self = this.player(seat);
        const wolf = isWolfRole(self.role);
        const agreed = wolf ? this.majorityTarget() : undefined;
        return {
            day: this.day,
            phase: this.phase,
            prompt: this.prompt,
            seats: this.players.map((player) => ({
                seat: player.seat,
                name: player.name,
                alive: player.alive,
                deadReason: player.deadReason,
                isSheriff: player.isSheriff,
                campaigning: player.campaigning,
                revealedIdiot: player.revealedIdiot,
                wolfMate: wolf && isWolfRole(player.role),
                knifeBy: wolf ? this.knivesOn(player.seat) : [],
                role: this.phase === 'settlement' ? player.role : null,
            })),
            selfSeat: seat,
            selfRole: self.role,
            actions: this.actionsFor(seat),
            logs: this.logs.filter((entry) => this.canSee(self, entry)),
            winner: this.winner,
            urgent: this.isUrgent(),
            notice: this.notices[seat] || null,
            agreedKnife: agreed,
            wolfPicks: wolf && this.phase === 'night_wolf' ? this.wolfPickList() : [],
            ballots: this.voteBoard,
            voteText: this.voteText,
        };
    }

    getPicker(seat: number, action: string): PickerState | null {
        const player = this.player(seat);
        if (action === 'guard') {
            return {
                title: '选择守护目标',
                options: this.withEmpty(this.livingSeats().filter((target) => target !== this.lastGuard[seat]), '空守'),
            };
        }
        if (action === 'knife') {
            return { title: '选择刀人目标', options: this.withEmpty(this.livingSeats(), '空刀') };
        }
        if (action === 'seer' || action === 'poison') {
            return {
                title: action === 'seer' ? '选择查验目标' : '选择下毒目标',
                options: this.livingSeats().map((target) => ({ target, label: this.seatLabel(target) })),
            };
        }
        if (action === 'vote') {
            const options = this.voteCandidates.map((target) => ({ target, label: this.seatLabel(target) }));
            if (this.allowAbstain) {
                options.push({ target: null, label: '弃权' });
            }
            return { title: this.voteKind === 'sheriff' ? '投票选警长' : '投票放逐', options };
        }
        if (action === 'badge') {
            const options = this.livingSeats()
                .filter((target) => target !== player.seat)
                .map((target) => ({ target, label: this.seatLabel(target) }));
            options.push({ target: null, label: '撕毁警徽' });
            return { title: '选择警徽流', options };
        }
        if (action === 'shoot') {
            return {
                title: '猎人开枪',
                options: this.withEmpty(this.livingSeats(), '不开枪'),
            };
        }
        if (action === 'explode') {
            return {
                title: '白狼王自爆',
                options: this.withEmpty(this.livingSeats().filter((target) => target !== player.seat), '不带走'),
            };
        }
        return null;
    }

    private apply(seat: number, action: GameAction): ActionResult {
        if (this.winner) {
            return { ok: false, error: '对局已结束' };
        }
        const player = this.players[seat - 1];
        if (!player) {
            return { ok: false, error: '座位不存在' };
        }
        switch (action.type) {
            case 'guard':
                return this.doGuard(player, action.target);
            case 'knife':
                return this.doKnife(player, action.target);
            case 'knife_confirm':
                return this.doKnifeConfirm(player);
            case 'seer':
                return this.doSeer(player, action.target);
            case 'poison':
                return this.doPoison(player, action.target);
            case 'antidote':
                return this.doAntidote(player);
            case 'witch_pass':
                return this.doWitchPass(player);
            case 'campaign':
                return this.doCampaign(player);
            case 'step_down':
                return this.doStepDown(player);
            case 'start_speech':
                return this.doStartSpeech(player);
            case 'start_vote':
                return this.doStartVote(player);
            case 'vote':
                return this.doVote(player, action.target);
            case 'badge':
                return this.doBadge(player, action.target);
            case 'shoot':
                return this.doShoot(player, action.target);
            case 'explode':
                return this.doExplode(player, action.target);
            case 'reveal_idiot':
                return this.doReveal(player);
            case 'accept_exile':
                return this.doAcceptExile(player);
            default:
                return { ok: false, error: '未知操作' };
        }
    }

    private doGuard(player: PlayerState, target: number | null): ActionResult {
        if (this.phase !== 'night_guard' || !player.alive || player.role !== 'guard') {
            return { ok: false, error: '现在不能守护' };
        }
        if (this.guardChoices[player.seat] !== undefined) {
            return { ok: false, error: '已经守护过了' };
        }
        if (target != null && (!this.isAliveSeat(target) || target === this.lastGuard[player.seat])) {
            return { ok: false, error: '不能连续两晚守护同一名玩家' };
        }
        this.guardChoices[player.seat] = target;
        this.lastGuard[player.seat] = target;
        this.addLog('guard', target == null ? `${player.seat}号空守` : `${player.seat}号守护了${target}号`);
        const waiting = this.livingWhere((item) => item.role === 'guard')
            .some((item) => this.guardChoices[item.seat] === undefined);
        if (!waiting) {
            this.enterWolf();
        }
        return { ok: true };
    }

    private doKnife(player: PlayerState, target: number | null): ActionResult {
        if (this.phase !== 'night_wolf' || !player.alive || !isWolfRole(player.role) || this.wolfLocked) {
            return { ok: false, error: '现在不能给刀' };
        }
        if (target != null && !this.isAliveSeat(target)) {
            return { ok: false, error: '目标已出局' };
        }
        if (this.wolfChoices[player.seat] === target) {
            return { ok: false, error: '已经选择这个目标' };
        }
        this.wolfChoices[player.seat] = target;
        return { ok: true };
    }

    private doKnifeConfirm(player: PlayerState): ActionResult {
        if (this.phase !== 'night_wolf' || !player.alive || !isWolfRole(player.role) || this.wolfLocked) {
            return { ok: false, error: '现在不能确定' };
        }
        if (this.now < this.wolfSettleAt) {
            return { ok: false, error: '请等待其他狼人选择' };
        }
        if (this.majorityTarget() === undefined) {
            return { ok: false, error: '还没有足够的狼人选择同一目标' };
        }
        this.lockKnife();
        return { ok: true };
    }

    private doSeer(player: PlayerState, target: number): ActionResult {
        if (this.phase !== 'night_seer' || !player.alive || player.role !== 'seer') {
            return { ok: false, error: '现在不能查验' };
        }
        if (this.seerPending.indexOf(player.seat) < 0) {
            return { ok: false, error: '已经查验过了' };
        }
        if (!this.isAliveSeat(target)) {
            return { ok: false, error: '目标已出局' };
        }
        const targetPlayer = this.player(target);
        const good = !isWolfRole(targetPlayer.role);
        const side = good ? '好人' : '狼人';
        this.addLog('seer', `查验了${target}号，是${side}`);
        this.notices[player.seat] = { id: this.noticeSeq++, text: `${target}号是${side}` };
        this.seerPending = this.seerPending.filter((item) => item !== player.seat);
        if (this.seerPending.length === 0) {
            this.enterWitch();
        }
        return { ok: true };
    }

    private doPoison(player: PlayerState, target: number): ActionResult {
        if (!this.canWitchAct(player) || this.poisonUsed) {
            return { ok: false, error: '现在不能使用毒药' };
        }
        if (!this.isAliveSeat(target)) {
            return { ok: false, error: '目标已出局' };
        }
        this.poisonUsed = true;
        this.poisonTarget = target;
        this.addLog('witch', `对${target}号使用了毒药`);
        this.finishWitch(player.seat);
        return { ok: true };
    }

    private doAntidote(player: PlayerState): ActionResult {
        if (!this.canWitchAct(player) || this.antidoteUsed || !this.canSave()) {
            return { ok: false, error: '现在不能使用解药' };
        }
        this.antidoteUsed = true;
        this.witchSaved = true;
        this.addLog('witch', `对${this.knifeTarget}号使用了解药`);
        this.finishWitch(player.seat);
        return { ok: true };
    }

    private doWitchPass(player: PlayerState): ActionResult {
        if (!this.canWitchAct(player)) {
            return { ok: false, error: '现在不能结束行动' };
        }
        this.finishWitch(player.seat);
        return { ok: true };
    }

    private doCampaign(player: PlayerState): ActionResult {
        if (this.phase !== 'day_campaign' || !player.alive) {
            return { ok: false, error: '现在不能上警' };
        }
        if (player.campaigning) {
            return { ok: false, error: '已经上警' };
        }
        player.campaigning = true;
        const text = `${this.campaigners().map((item) => `${item.seat}号`).join('，')}玩家上警`;
        const entry = this.logs.find((item) => item.id === this.campaignLogId);
        if (entry) {
            entry.text = text;
        } else {
            this.addLog('public', text);
            this.campaignLogId = this.logSeq - 1;
        }
        return { ok: true };
    }

    private doStepDown(player: PlayerState): ActionResult {
        if (this.phase !== 'day_campaign_speech' || !player.alive || !player.campaigning) {
            return { ok: false, error: '现在不能下警' };
        }
        player.campaigning = false;
        this.addLog('public', `${player.seat}号玩家下警`);
        this.tryFinishCampaign();
        return { ok: true };
    }

    private doStartSpeech(player: PlayerState): ActionResult {
        if (player.seat !== 1) {
            return { ok: false, error: '只有1号可以开始发言' };
        }
        if (this.phase === 'day_campaign') {
            this.enterCampaignSpeech();
            return { ok: true };
        }
        if (this.phase === 'day_talk_ready') {
            this.phase = 'day_talk';
            this.prompt = this.diedThisDay ? '开始从死左或者死右依次发言' : '开始从警左或警右依次发言';
            this.hold();
            return { ok: true };
        }
        return { ok: false, error: '现在不能开始发言' };
    }

    private doStartVote(player: PlayerState): ActionResult {
        if (player.seat !== 1) {
            return { ok: false, error: '只有1号可以开始投票' };
        }
        if (this.phase === 'day_campaign_speech') {
            const camp = this.campaigners();
            if (camp.length <= 1) {
                this.tryFinishCampaign();
                return { ok: true };
            }
            this.openVote('sheriff', this.livingWhere((item) => !item.campaigning).map((item) => item.seat), camp.map((item) => item.seat), true);
            this.prompt = '请开始为警长投票';
            return { ok: true };
        }
        if (this.phase === 'day_talk') {
            this.openVote('exile', this.livingSeats(), this.livingSeats(), true);
            this.prompt = '请开始放逐投票';
            return { ok: true };
        }
        return { ok: false, error: '现在不能开始投票' };
    }

    private doVote(player: PlayerState, target: number | null): ActionResult {
        const votePhase = this.phase === 'day_sheriff_vote' || this.phase === 'day_exile_vote';
        if (!votePhase || !player.alive || this.voters.indexOf(player.seat) < 0) {
            return { ok: false, error: '现在不能投票' };
        }
        if (this.votes[player.seat] !== undefined) {
            return { ok: false, error: '已经投过票了' };
        }
        if (target == null && !this.allowAbstain) {
            return { ok: false, error: '不能弃权' };
        }
        if (target != null && this.voteCandidates.indexOf(target) < 0) {
            return { ok: false, error: '不能投给这个目标' };
        }
        this.votes[player.seat] = target;
        this.syncVoteBoard();
        if (this.voters.every((item) => this.votes[item] !== undefined)) {
            this.resolveVote();
        }
        return { ok: true };
    }

    private doBadge(player: PlayerState, target: number | null): ActionResult {
        if (this.phase !== 'resolve_badge' || !this.current || this.current.seat !== player.seat) {
            return { ok: false, error: '现在不能移交警徽' };
        }
        if (target != null && (!this.isAliveSeat(target) || target === player.seat)) {
            return { ok: false, error: '不能把警徽给这个玩家' };
        }
        this.transferBadge(player.seat, target);
        if (this.badgeOnly) {
            this.badgeOnly = false;
            this.current = null;
            this.goNextNight();
            return { ok: true };
        }
        this.finishCurrentDeath();
        if (!this.current) {
            this.pumpQueue();
        }
        return { ok: true };
    }

    private doShoot(player: PlayerState, target: number | null): ActionResult {
        if (this.phase !== 'resolve_hunter' || !this.current || this.current.seat !== player.seat) {
            return { ok: false, error: '现在不能开枪' };
        }
        if (target != null && !this.isAliveSeat(target)) {
            return { ok: false, error: '目标已出局' };
        }
        player.shotUsed = true;
        const fromExile = this.current.cause === 'exile';
        this.current = null;
        if (target != null) {
            this.addLog('self', `你射杀了${target}号玩家`, player.seat);
            if (fromExile) {
                this.killSilently(target);
                this.carriedDeaths.push(target);
            } else {
                this.queue.push({ seat: target, reason: 'killed', cause: 'shot' });
            }
        }
        this.pumpQueue();
        return { ok: true };
    }

    private doExplode(player: PlayerState, target: number | null): ActionResult {
        if (player.role !== 'whiteWolf' || !player.alive || DAY_EXPLODE.indexOf(this.phase) < 0) {
            return { ok: false, error: '现在不能自爆' };
        }
        if (target != null && (!this.isAliveSeat(target) || target === player.seat)) {
            return { ok: false, error: '不能带走这个玩家' };
        }
        this.addLog('public', target == null
            ? `${player.seat}号白狼王自爆，没有带走玩家`
            : `${player.seat}号白狼王自爆，带走了${target}号`);
        this.voters = [];
        this.votes = {};
        this.badgeOnly = false;
        this.queue = [{ seat: player.seat, reason: 'killed', cause: 'explode' }];
        if (target != null) {
            this.queue.push({ seat: target, reason: 'killed', cause: 'explode' });
        }
        this.current = null;
        this.afterQueue = 'night';
        this.waitingDay = false;
        this.pumpQueue();
        return { ok: true };
    }

    private doReveal(player: PlayerState): ActionResult {
        if (this.phase !== 'day_idiot' || player.seat !== this.idiotSeat) {
            return { ok: false, error: '现在不能亮身份' };
        }
        player.revealedIdiot = true;
        this.addLog('public', `${player.seat}号玩家亮明身份，是白痴，不会被放逐`);
        if (player.isSheriff) {
            this.badgeOnly = true;
            this.current = { seat: player.seat, reason: 'exiled', cause: 'exile' };
            this.phase = 'resolve_badge';
            this.prompt = `${player.seat}号请选择警徽流`;
            this.arm(ACTION_TIMEOUT_MS);
            return { ok: true };
        }
        this.goNextNight();
        return { ok: true };
    }

    private doAcceptExile(player: PlayerState): ActionResult {
        if (this.phase !== 'day_idiot' || player.seat !== this.idiotSeat) {
            return { ok: false, error: '现在不能接受放逐' };
        }
        this.queue = [{ seat: player.seat, reason: 'exiled', cause: 'exile' }];
        this.current = null;
        this.afterQueue = 'night';
        this.pumpQueue();
        return { ok: true };
    }

    private onTimeout(): void {
        if (this.phase === 'night_guard') {
            this.livingWhere((player) => player.role === 'guard').forEach((player) => {
                if (this.guardChoices[player.seat] === undefined) {
                    this.apply(player.seat, { type: 'guard', target: null });
                }
            });
            return;
        }
        if (this.phase === 'night_wolf') {
            this.forceWolf();
            return;
        }
        if (this.phase === 'night_seer') {
            this.seerPending = [];
            this.enterWitch();
            return;
        }
        if (this.phase === 'night_witch') {
            this.witchPending = [];
            this.enterDawn();
            return;
        }
        if (this.phase === 'dawn_announce') {
            this.tryEnterDay();
            return;
        }
        if (this.phase === 'resolve_badge' && this.current) {
            this.apply(this.current.seat, { type: 'badge', target: null });
            return;
        }
        if (this.phase === 'resolve_hunter' && this.current) {
            this.apply(this.current.seat, { type: 'shoot', target: null });
            return;
        }
        if (this.phase === 'day_sheriff_vote' || this.phase === 'day_exile_vote') {
            this.voters.forEach((seat) => {
                if (this.votes[seat] === undefined) {
                    this.votes[seat] = null;
                }
            });
            this.resolveVote();
            return;
        }
        if (this.phase === 'day_idiot') {
            this.apply(this.idiotSeat, { type: 'accept_exile' });
        }
    }

    private beginNight(): void {
        this.guardChoices = {};
        this.wolfChoices = {};
        this.wolfLocked = false;
        this.knifeTarget = null;
        this.witchSaved = false;
        this.poisonTarget = null;
        this.diedThisDay = false;
        this.waitingDay = false;
        this.voters = [];
        this.votes = {};
        this.voteBoard = [];
        this.voteText = '';
        this.current = null;
        this.queue = [];
        this.badgeOnly = false;
        if (this.day === 1) {
            this.announceWolves();
        }
        this.enterGuard();
    }

    private enterGuard(): void {
        const living = this.livingWhere((player) => player.role === 'guard');
        if (living.length === 0) {
            this.enterWolf();
            return;
        }
        this.phase = 'night_guard';
        this.prompt = '守卫请睁眼，请选择你要守护的人';
        this.arm(ACTION_TIMEOUT_MS);
    }

    private enterWolf(): void {
        const wolves = this.livingWolves();
        if (wolves.length === 0) {
            this.enterSeer();
            return;
        }
        this.phase = 'night_wolf';
        this.prompt = '守卫请闭眼，狼人请睁眼，请选择你要刀的人';
        this.wolfChoices = {};
        this.wolfLocked = false;
        this.wolfSettleAt = this.now + KNIFE_SETTLE_MS;
        this.arm(Math.max(ACTION_TIMEOUT_MS, KNIFE_SETTLE_MS));
    }

    private enterSeer(): void {
        const seers = this.players.filter((player) => player.role === 'seer');
        if (seers.length === 0) {
            this.enterWitch();
            return;
        }
        this.phase = 'night_seer';
        this.prompt = '狼人请闭眼，预言家请睁眼，请选择你要查验的人';
        this.seerPending = seers.filter((player) => player.alive).map((player) => player.seat);
        this.arm(this.seerPending.length === 0 ? DEAD_SKIP_MS : ACTION_TIMEOUT_MS);
    }

    private enterWitch(): void {
        const witches = this.players.filter((player) => player.role === 'witch');
        if (witches.length === 0) {
            this.enterDawn();
            return;
        }
        this.phase = 'night_witch';
        this.prompt = '预言家请闭眼，女巫请睁眼，请选择你要毒/救的人';
        this.witchPending = witches.filter((player) => player.alive).map((player) => player.seat);
        this.arm(this.witchPending.length === 0 ? DEAD_SKIP_MS : ACTION_TIMEOUT_MS);
    }

    private enterDawn(): void {
        const deaths = this.computeNightDeaths();
        const carried = this.carriedDeaths.splice(0);
        const seats = this.uniqueSeats(deaths.map((item) => item.seat).concat(carried));
        this.dawnPrompt = seats.length === 0
            ? '女巫请闭眼，天亮了，昨晚是平安夜'
            : `女巫请闭眼，天亮了，昨晚淘汰的玩家是${seats.map((seat) => `${seat}号`).join('、')}`;
        this.prompt = this.dawnPrompt;
        this.addLog('public', this.dawnPrompt);
        carried.forEach((seat) => {
            this.addLog('public', `${seat}号玩家被淘汰`);
            this.diedThisDay = true;
        });
        this.dawnUnlockAt = this.now + (this.day === 1 ? DAWN_WAIT_MS : 0);
        this.waitingDay = true;
        this.queue = deaths;
        this.current = null;
        this.afterQueue = 'day';
        this.phase = 'dawn_announce';
        this.deadline = null;
        this.pumpQueue();
    }

    private tryEnterDay(): void {
        if (!this.waitingDay || this.winner) {
            return;
        }
        if (this.now < this.dawnUnlockAt) {
            this.phase = 'dawn_announce';
            this.prompt = this.dawnPrompt;
            this.deadline = this.dawnUnlockAt;
            return;
        }
        this.waitingDay = false;
        if (this.day === 1 && !this.sheriffDone) {
            this.phase = 'day_campaign';
            this.prompt = '开始竞选警长';
            this.hold();
            return;
        }
        this.phase = 'day_talk_ready';
        this.prompt = this.dawnPrompt;
        this.hold();
    }

    private enterCampaignSpeech(): void {
        this.phase = 'day_campaign_speech';
        this.prompt = '警上请按顺序发言';
        if (this.tryFinishCampaign()) {
            return;
        }
        this.hold();
    }

    private tryFinishCampaign(): boolean {
        const camp = this.campaigners();
        if (camp.length > 1) {
            return false;
        }
        const seat = camp.length === 1 ? camp[0].seat : this.lowestLiving();
        this.makeSheriff(seat);
        return true;
    }

    private makeSheriff(seat: number): void {
        this.players.forEach((player) => {
            player.isSheriff = player.seat === seat;
            player.campaigning = false;
        });
        this.sheriffDone = true;
        this.addLog('public', `${seat}号玩家当选警长`);
        this.phase = 'day_talk_ready';
        this.prompt = `${seat}号玩家当选警长`;
        this.hold();
    }

    private openVote(kind: 'sheriff' | 'exile', voters: number[], candidates: number[], abstain: boolean): void {
        this.voteKind = kind;
        this.voters = voters.slice();
        this.voteCandidates = candidates.slice();
        this.allowAbstain = abstain;
        this.votes = {};
        this.voteBoard = [];
        this.voteText = '';
        this.phase = kind === 'sheriff' ? 'day_sheriff_vote' : 'day_exile_vote';
        this.arm(ACTION_TIMEOUT_MS);
        if (this.voters.length === 0) {
            this.resolveVote();
        }
    }

    private resolveVote(): void {
        const kind = this.voteKind;
        const summary = this.formatVote(kind === 'sheriff' ? '竞选警长' : '放逐投票');
        this.voteText = summary;
        this.voteBoard = this.voters.map((seat) => ({ seat, target: this.votes[seat] ?? null }));
        this.addLog('public', summary);
        const scores: { [seat: number]: number } = {};
        this.voters.forEach((seat) => {
            const target = this.votes[seat];
            if (target == null) {
                return;
            }
            const weight = kind === 'exile' && this.player(seat).isSheriff ? 3 : 2;
            scores[target] = (scores[target] || 0) + weight;
        });
        const ranked = Object.keys(scores).map((key) => ({ seat: Number(key), score: scores[Number(key)] }));
        ranked.sort((a, b) => b.score - a.score);
        const highest = ranked.length ? ranked[0].score : 0;
        const top = ranked.filter((item) => item.score === highest);
        if (top.length === 0) {
            if (kind === 'sheriff') {
                this.makeSheriff(this.lowestLiving());
            } else {
                this.addLog('public', '本轮投票无人出局');
                this.goNextNight();
            }
            return;
        }
        if (top.length > 1) {
            this.votes = {};
            this.voteBoard = [];
            this.voteText = '';
            this.prompt = kind === 'sheriff' ? '警长票数相同，请再次投票' : '放逐票数相同，请再次投票';
            this.arm(ACTION_TIMEOUT_MS);
            return;
        }
        const winner = top[0].seat;
        if (kind === 'sheriff') {
            this.makeSheriff(winner);
            return;
        }
        const exiled = this.player(winner);
        if (exiled.role === 'idiot' && !exiled.revealedIdiot) {
            this.idiotSeat = winner;
            this.phase = 'day_idiot';
            this.prompt = `${winner}号被投票放逐，可以亮明身份`;
            this.arm(ACTION_TIMEOUT_MS);
            return;
        }
        this.queue = [{ seat: winner, reason: 'exiled', cause: 'exile' }];
        this.current = null;
        this.afterQueue = 'night';
        this.pumpQueue();
    }

    private pumpQueue(): void {
        if (this.winner) {
            return;
        }
        while (!this.current && this.queue.length > 0) {
            const next = this.queue.shift()!;
            const player = this.player(next.seat);
            if (!player.alive) {
                continue;
            }
            this.current = next;
            if (player.isSheriff) {
                this.phase = 'resolve_badge';
                this.prompt = `${player.seat}号请选择警徽流`;
                this.arm(ACTION_TIMEOUT_MS);
                return;
            }
            this.finishCurrentDeath();
            if (this.current || this.winner) {
                return;
            }
        }
        if (!this.current && this.queue.length === 0) {
            this.onQueueDone();
        }
    }

    private finishCurrentDeath(): void {
        if (!this.current) {
            return;
        }
        const pending = this.current;
        const player = this.player(pending.seat);
        this.applyDeath(player, pending.reason);
        if (this.canHunterShoot(player, pending.cause)) {
            this.phase = 'resolve_hunter';
            this.prompt = `${player.seat}号猎人请选择开枪目标`;
            this.arm(ACTION_TIMEOUT_MS);
            return;
        }
        this.current = null;
    }

    private onQueueDone(): void {
        const next = this.afterQueue;
        this.afterQueue = null;
        if (this.checkWin()) {
            return;
        }
        if (next === 'day') {
            this.tryEnterDay();
        } else if (next === 'night') {
            this.goNextNight();
        }
    }

    private applyDeath(player: PlayerState, reason: 'killed' | 'exiled'): void {
        player.alive = false;
        player.deadReason = reason === 'exiled' ? 'exiled' : 'killed';
        player.isSheriff = false;
        player.campaigning = false;
        this.diedThisDay = true;
        this.addLog('public', reason === 'exiled' ? `${player.seat}号玩家被放逐` : `${player.seat}号玩家被淘汰`);
    }

    private goNextNight(): void {
        if (this.checkWin()) {
            return;
        }
        this.day += 1;
        this.beginNight();
    }

    private checkWin(): boolean {
        const alive = (camp: 'villager' | 'god' | 'wolf') => this.players.some((player) => player.alive && campOf(player.role) === camp);
        if (this.wolfCount > 0 && !alive('wolf')) {
            this.finish('good');
            return true;
        }
        if ((this.villagerCount > 0 && !alive('villager')) || (this.godCount > 0 && !alive('god'))) {
            this.finish('wolf');
            return true;
        }
        return false;
    }

    private finish(winner: 'good' | 'wolf'): void {
        this.winner = winner;
        this.phase = 'settlement';
        this.prompt = winner === 'good' ? '好人阵营获胜' : '狼人阵营获胜';
        this.deadline = null;
        this.queue = [];
        this.current = null;
        this.flushCarriedDeaths();
        this.addLog('public', this.prompt);
    }

    private computeNightDeaths(): PendingDeath[] {
        const deaths: PendingDeath[] = [];
        const knifeLands = this.knifeTarget != null
            && !this.wasGuarded(this.knifeTarget)
            && !this.witchSaved;
        if (knifeLands && this.knifeTarget != null) {
            deaths.push({ seat: this.knifeTarget, reason: 'killed', cause: 'knife' });
        }
        if (this.poisonTarget != null) {
            const existing = deaths.find((item) => item.seat === this.poisonTarget);
            if (existing) {
                existing.cause = 'poison';
            } else {
                deaths.push({ seat: this.poisonTarget, reason: 'killed', cause: 'poison' });
            }
        }
        return deaths;
    }

    private canHunterShoot(player: PlayerState, cause: DeathCause): boolean {
        return player.role === 'hunter' && !player.shotUsed && (cause === 'knife' || cause === 'exile');
    }

    private killSilently(seat: number): void {
        const player = this.player(seat);
        if (!player.alive) {
            return;
        }
        player.alive = false;
        player.deadReason = 'killed';
        player.isSheriff = false;
        player.campaigning = false;
    }

    private flushCarriedDeaths(): void {
        const carried = this.carriedDeaths.splice(0);
        carried.forEach((seat) => {
            this.addLog('public', `${seat}号玩家被淘汰`);
        });
    }

    private uniqueSeats(seats: number[]): number[] {
        const seen: { [seat: number]: boolean } = {};
        const list: number[] = [];
        seats.forEach((seat) => {
            if (seen[seat]) {
                return;
            }
            seen[seat] = true;
            list.push(seat);
        });
        list.sort((a, b) => a - b);
        return list;
    }

    private lockKnife(): void {
        const decided = this.majorityTarget();
        this.wolfLocked = true;
        if (decided === undefined) {
            this.knifeTarget = null;
            this.addLog('wolf', '狼人未能达成刀口');
        } else if (decided === null) {
            this.knifeTarget = null;
            this.addLog('wolf', '狼人空刀');
        } else {
            this.knifeTarget = decided;
            this.addLog('wolf', `刀了${decided}号玩家`);
        }
        this.enterSeer();
    }

    private forceWolf(): void {
        this.lockKnife();
    }

    private announceWolves(): void {
        const wolves = this.players.filter((player) => isWolfRole(player.role));
        if (!wolves.length) {
            return;
        }
        const text = wolves.map((player) => `${player.seat}号是${roleInfo(player.role).name}`).join('，');
        this.addLog('wolf', text);
    }

    private finishWitch(seat: number): void {
        this.witchPending = this.witchPending.filter((item) => item !== seat);
        if (this.witchPending.length === 0) {
            this.enterDawn();
        }
    }

    private transferBadge(from: number, target: number | null): void {
        this.player(from).isSheriff = false;
        if (target == null) {
            this.addLog('public', `${from}号玩家撕毁了警徽`);
            return;
        }
        this.player(target).isSheriff = true;
        this.addLog('public', `${from}号玩家将警徽交给了${target}号`);
    }

    private syncVoteBoard(): void {
        this.voteBoard = this.voters
            .filter((seat) => this.votes[seat] !== undefined)
            .map((seat) => ({ seat, target: this.votes[seat] ?? null }));
        this.voteText = this.voteBoard.length
            ? this.formatVote(this.voteKind === 'sheriff' ? '竞选警长' : '放逐投票', true)
            : '';
    }

    private formatVote(title: string, onlyCast = false): string {
        const grouped: { [seat: number]: number[] } = {};
        const abstain: number[] = [];
        this.voters.forEach((seat) => {
            if (onlyCast && this.votes[seat] === undefined) {
                return;
            }
            const target = this.votes[seat];
            if (target == null) {
                abstain.push(seat);
                return;
            }
            if (!grouped[target]) {
                grouped[target] = [];
            }
            grouped[target].push(seat);
        });
        const parts = Object.keys(grouped)
            .map((key) => Number(key))
            .sort((a, b) => a - b)
            .map((target) => `${grouped[target].sort((a, b) => a - b).map((seat) => `${seat}号`).join('，')}投给了${target}号`);
        if (abstain.length) {
            parts.push(`${abstain.sort((a, b) => a - b).map((seat) => `${seat}号`).join('，')}玩家弃权`);
        }
        return `${title}：${parts.join('；') || '无人投票'}`;
    }

    private actionsFor(seat: number): { id: string; label: string; urgent: boolean }[] {
        const player = this.player(seat);
        const urgent = this.isUrgent();
        const list: { id: string; label: string; urgent: boolean }[] = [];
        const push = (id: string, label: string) => list.push({ id, label, urgent });
        if (this.phase === 'night_guard' && player.alive && player.role === 'guard' && this.guardChoices[seat] === undefined) {
            push('guard', '守护');
        }
        if (this.phase === 'night_wolf' && player.alive && isWolfRole(player.role) && !this.wolfLocked) {
            push('knife', '给刀');
            if (this.knifeConfirmReady()) {
                push('knife_confirm', '确定');
            }
        }
        if (this.phase === 'night_seer' && player.alive && player.role === 'seer' && this.seerPending.indexOf(seat) >= 0) {
            push('seer', '查验');
        }
        if (this.phase === 'night_witch' && this.canWitchAct(player)) {
            if (!this.poisonUsed) {
                push('poison', '毒药');
            }
            if (!this.antidoteUsed && this.canSave()) {
                push('antidote', '解药');
            }
            push('witch_pass', '结束行动');
        }
        if (this.phase === 'day_campaign' && player.alive) {
            push('campaign', '上警');
        }
        if (this.phase === 'day_campaign_speech' && player.alive && player.campaigning) {
            push('step_down', '下警');
        }
        if (seat === 1 && (this.phase === 'day_campaign' || this.phase === 'day_talk_ready')) {
            push('start_speech', '开始发言');
        }
        if (seat === 1 && (this.phase === 'day_campaign_speech' || this.phase === 'day_talk')) {
            push('start_vote', '开始投票');
        }
        if ((this.phase === 'day_sheriff_vote' || this.phase === 'day_exile_vote')
            && player.alive
            && this.voters.indexOf(seat) >= 0
            && this.votes[seat] === undefined) {
            push('vote', '投票');
        }
        if (this.phase === 'resolve_badge' && this.current && this.current.seat === seat) {
            push('badge', '警徽流');
        }
        if (this.phase === 'resolve_hunter' && this.current && this.current.seat === seat) {
            push('shoot', '开枪');
        }
        if (this.phase === 'day_idiot' && seat === this.idiotSeat) {
            push('reveal_idiot', '亮身份');
            push('accept_exile', '接受放逐');
        }
        if (player.alive && player.role === 'whiteWolf' && DAY_EXPLODE.indexOf(this.phase) >= 0) {
            push('explode', '自爆');
        }
        return list.map((item) => {
            const picker = this.getPicker(seat, item.id);
            return picker ? { ...item, options: picker.options } : item;
        });
    }

    private canWitchAct(player: PlayerState): boolean {
        return this.phase === 'night_witch'
            && player.alive
            && player.role === 'witch'
            && this.witchPending.indexOf(player.seat) >= 0;
    }

    private canSave(): boolean {
        return this.knifeTarget != null && !this.wasGuarded(this.knifeTarget) && !this.antidoteUsed;
    }

    private wasGuarded(seat: number | null): boolean {
        if (seat == null) {
            return false;
        }
        return this.players.some((player) => player.role === 'guard' && this.guardChoices[player.seat] === seat);
    }

    private knifeConfirmReady(): boolean {
        return this.phase === 'night_wolf'
            && !this.wolfLocked
            && this.now >= this.wolfSettleAt
            && this.majorityTarget() !== undefined;
    }

    private majorityTarget(): number | null | undefined {
        const wolves = this.livingWolves();
        const total = wolves.length;
        if (!total || this.wolfLocked) {
            return undefined;
        }
        const counts: { [key: string]: number } = {};
        wolves.forEach((wolf) => {
            const choice = this.wolfChoices[wolf.seat];
            if (choice === undefined) {
                return;
            }
            const key = choice == null ? 'empty' : String(choice);
            counts[key] = (counts[key] || 0) + 1;
        });
        const keys = Object.keys(counts);
        for (let index = 0; index < keys.length; index += 1) {
            const key = keys[index];
            const count = counts[key];
            const empty = key === 'empty';
            const enough = empty ? count * 2 >= total : count * 2 > total;
            if (enough) {
                return empty ? null : Number(key);
            }
        }
        return undefined;
    }

    private wolfPickList(): { seat: number; target: number | null }[] {
        return this.livingWolves()
            .filter((wolf) => this.wolfChoices[wolf.seat] !== undefined)
            .map((wolf) => ({ seat: wolf.seat, target: this.wolfChoices[wolf.seat] ?? null }));
    }

    private knivesOn(target: number): number[] {
        if (this.phase !== 'night_wolf' || this.wolfLocked) {
            return [];
        }
        return this.livingWolves()
            .filter((wolf) => this.wolfChoices[wolf.seat] === target)
            .map((wolf) => wolf.seat);
    }

    private campaigners(): PlayerState[] {
        return this.livingWhere((player) => player.campaigning);
    }

    private lowestLiving(): number {
        const seats = this.livingSeats();
        return seats.length ? seats[0] : 1;
    }

    private livingWolves(): PlayerState[] {
        return this.livingWhere((player) => isWolfRole(player.role));
    }

    private livingWhere(test: (player: PlayerState) => boolean): PlayerState[] {
        return this.players.filter((player) => player.alive && test(player));
    }

    private livingSeats(): number[] {
        return this.players.filter((player) => player.alive).map((player) => player.seat);
    }

    private isAliveSeat(seat: number): boolean {
        const player = this.players[seat - 1];
        return !!player && player.alive;
    }

    private withEmpty(seats: number[], emptyLabel: string): { target: number | null; label: string }[] {
        return seats.map((target) => ({ target, label: this.seatLabel(target) }))
            .concat([{ target: null, label: emptyLabel }]);
    }

    private seatLabel(seat: number): string {
        const player = this.player(seat);
        return `${seat}号-${player.name}`;
    }

    private canSee(player: PlayerState, entry: LogEntry): boolean {
        if (entry.audience === 'self') {
            return entry.seat === player.seat;
        }
        if (entry.audience === 'public') {
            return true;
        }
        if (entry.audience === 'wolf') {
            return isWolfRole(player.role);
        }
        if (entry.audience === 'seer') {
            return player.role === 'seer';
        }
        if (entry.audience === 'witch') {
            return player.role === 'witch';
        }
        return player.role === 'guard';
    }

    private addLog(audience: LogAudience, text: string, seat?: number): void {
        const entry: LogEntry = { id: this.logSeq++, day: this.day, audience, text };
        if (seat != null) {
            entry.seat = seat;
        }
        this.logs.push(entry);
    }

    private arm(ms: number): void {
        this.deadline = this.now + ms;
    }

    private hold(): void {
        this.deadline = null;
    }

    private touch(): void {
        this.rev += 1;
    }

    private player(seat: number): PlayerState {
        return this.players[seat - 1];
    }
}

export function roleName(id: RoleId): string {
    return roleInfo(id).name;
}
