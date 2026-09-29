import { GameAction, GameView, RoleId } from '../game/types';

export interface RoomSeat {
    seat: number;
    name: string;
    filled: boolean;
}

export interface RoomState {
    roomId: string;
    hostSeat: number;
    playerCount: number;
    roles: RoleId[];
    seats: RoomSeat[];
    stage: 'lobby' | 'playing' | 'settlement';
    selfSeat: number;
}

export type ClientMessage =
    | { type: 'create_room'; name: string }
    | { type: 'join_room'; roomId: string; name: string; token?: string }
    | { type: 'set_config'; playerCount: number; roles: RoleId[] }
    | { type: 'start_game' }
    | { type: 'action'; action: GameAction }
    | { type: 'leave' };

export type ServerMessage =
    | { type: 'hello'; roomId: string; seat: number; token: string }
    | { type: 'room_state'; room: RoomState }
    | { type: 'game_view'; view: GameView }
    | { type: 'error'; message: string };
