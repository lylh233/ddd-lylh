import { RoleId } from './types';

export const MIN_PLAYER_COUNT = 4;
export const MAX_PLAYER_COUNT = 16;

export function isPlayerCount(value: number): boolean {
    return Number.isInteger(value) && value >= MIN_PLAYER_COUNT && value <= MAX_PLAYER_COUNT;
}

export function defaultRoles(count: number): RoleId[] {
    if (count === 4) {
        return ['werewolf', 'seer', 'witch', 'villager'];
    }
    if (count === 6) {
        return ['werewolf', 'werewolf', 'seer', 'witch', 'hunter', 'villager'];
    }
    if (count === 8) {
        return ['werewolf', 'werewolf', 'whiteWolf', 'seer', 'witch', 'hunter', 'guard', 'villager'];
    }
    if (count === 12) {
        return [
            'werewolf', 'werewolf', 'werewolf', 'whiteWolf',
            'seer', 'witch', 'hunter', 'guard', 'idiot',
            'villager', 'villager', 'villager',
        ];
    }
    const wolves = Math.max(1, Math.min(count - 2, Math.floor(count / 3)));
    const roles: RoleId[] = [];
    const white = wolves >= 3 ? 1 : 0;
    for (let index = 0; index < wolves - white; index += 1) {
        roles.push('werewolf');
    }
    if (white) {
        roles.push('whiteWolf');
    }
    const gods: RoleId[] = ['seer', 'witch', 'hunter', 'guard', 'idiot'];
    gods.forEach((role) => {
        if (count - roles.length > 1) {
            roles.push(role);
        }
    });
    while (roles.length < count) {
        roles.push('villager');
    }
    return roles;
}

export function shuffleRoles(roles: RoleId[], rng: () => number = Math.random): RoleId[] {
    const next = roles.slice();
    for (let index = next.length - 1; index > 0; index -= 1) {
        const swap = Math.floor(rng() * (index + 1));
        const current = next[index];
        next[index] = next[swap];
        next[swap] = current;
    }
    return next;
}
