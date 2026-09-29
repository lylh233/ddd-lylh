export type RoleId =
    | 'villager'
    | 'seer'
    | 'witch'
    | 'hunter'
    | 'guard'
    | 'idiot'
    | 'werewolf'
    | 'whiteWolf';

export type Camp = 'villager' | 'god' | 'wolf';

export type Phase =
    | 'night_guard'
    | 'night_wolf'
    | 'night_seer'
    | 'night_witch'
    | 'dawn_announce'
    | 'resolve_badge'
    | 'resolve_hunter'
    | 'day_campaign'
    | 'day_campaign_speech'
    | 'day_sheriff_vote'
    | 'day_talk_ready'
    | 'day_talk'
    | 'day_exile_vote'
    | 'day_idiot'
    | 'settlement';

export type LogAudience = 'public' | 'wolf' | 'seer' | 'witch' | 'guard' | 'self';

export type DeadReason = 'none' | 'killed' | 'exiled';

export interface RoleInfo {
    id: RoleId;
    name: string;
    desc: string;
    camp: Camp;
}

export const ROLE_LIST: RoleInfo[] = [
    { id: 'villager', name: '居民', desc: '普通居民，可投票发言', camp: 'villager' },
    { id: 'seer', name: '预言家', desc: '每晚可以查验一名玩家身份', camp: 'god' },
    { id: 'witch', name: '女巫', desc: '拥有一瓶解药和毒药', camp: 'god' },
    { id: 'hunter', name: '猎人', desc: '死亡时可以选择带走一名玩家', camp: 'god' },
    { id: 'guard', name: '守卫', desc: '每晚可以守护一名玩家', camp: 'god' },
    { id: 'idiot', name: '白痴', desc: '被放逐时可以翻牌继续游戏', camp: 'god' },
    { id: 'werewolf', name: '狼人', desc: '每晚可以杀死一名玩家', camp: 'wolf' },
    { id: 'whiteWolf', name: '白狼王', desc: '白天可以自爆带走一名玩家', camp: 'wolf' },
];

const ROLE_MAP: Record<string, RoleInfo> = {};
for (const role of ROLE_LIST) {
    ROLE_MAP[role.id] = role;
}

export function roleInfo(id: RoleId): RoleInfo {
    return ROLE_MAP[id];
}

export function campOf(id: RoleId): Camp {
    return ROLE_MAP[id].camp;
}

export function isWolfRole(id: RoleId): boolean {
    return ROLE_MAP[id].camp === 'wolf';
}

export function isRoleId(value: string): value is RoleId {
    return Object.prototype.hasOwnProperty.call(ROLE_MAP, value);
}

export interface LogEntry {
    id: number;
    day: number;
    audience: LogAudience;
    text: string;
    seat?: number;
}

export interface SeatView {
    seat: number;
    name: string;
    alive: boolean;
    deadReason: DeadReason;
    isSheriff: boolean;
    campaigning: boolean;
    revealedIdiot: boolean;
    wolfMate: boolean;
    knifeBy: number[];
    role: RoleId | null;
}

export interface ActionButton {
    id: string;
    label: string;
    urgent: boolean;
    options?: PickerOption[];
}

export interface PickerOption {
    target: number | null;
    label: string;
}

export interface PickerState {
    title: string;
    options: PickerOption[];
}

export interface Notice {
    id: number;
    text: string;
}

export interface GameView {
    day: number;
    phase: Phase;
    prompt: string;
    seats: SeatView[];
    selfSeat: number;
    selfRole: RoleId;
    actions: ActionButton[];
    logs: LogEntry[];
    winner: 'good' | 'wolf' | null;
    urgent: boolean;
    notice: Notice | null;
    agreedKnife: number | null | undefined;
    wolfPicks: { seat: number; target: number | null }[];
    ballots: { seat: number; target: number | null }[];
    voteText: string;
}

export type GameAction =
    | { type: 'guard'; target: number | null }
    | { type: 'knife'; target: number | null }
    | { type: 'knife_confirm' }
    | { type: 'seer'; target: number }
    | { type: 'poison'; target: number }
    | { type: 'antidote' }
    | { type: 'witch_pass' }
    | { type: 'campaign' }
    | { type: 'step_down' }
    | { type: 'start_speech' }
    | { type: 'start_vote' }
    | { type: 'vote'; target: number | null }
    | { type: 'badge'; target: number | null }
    | { type: 'shoot'; target: number | null }
    | { type: 'explode'; target: number | null }
    | { type: 'reveal_idiot' }
    | { type: 'accept_exile' };

export interface ActionResult {
    ok: boolean;
    error?: string;
}

export const ACTION_TIMEOUT_MS = 30000;
export const DEAD_SKIP_MS = 10000;
export const DAWN_WAIT_MS = 5000;
export const URGENT_MS = 5000;
export const KNIFE_SETTLE_MS = 20000;
