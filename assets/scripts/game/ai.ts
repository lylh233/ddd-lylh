import { GameEngine } from './GameEngine';
import { GameAction } from './types';

export function decideAi(engine: GameEngine, seat: number, rng: () => number = Math.random): GameAction | null {
    const view = engine.view(seat);
    const ids = view.actions.map((action) => action.id);
    if (!ids.length) {
        return null;
    }
    if (ids.indexOf('knife_confirm') >= 0) {
        return { type: 'knife_confirm' };
    }
    if (ids.indexOf('reveal_idiot') >= 0) {
        return { type: 'reveal_idiot' };
    }
    if (ids.indexOf('badge') >= 0) {
        return { type: 'badge', target: pick(engine, seat, 'badge', rng, 0.15) };
    }
    if (ids.indexOf('shoot') >= 0) {
        return { type: 'shoot', target: pick(engine, seat, 'shoot', rng, 0.1) };
    }
    if (ids.indexOf('antidote') >= 0 && rng() < 0.55) {
        return { type: 'antidote' };
    }
    if (ids.indexOf('poison') >= 0 && rng() < 0.35) {
        const options = engine.getPicker(seat, 'poison')!.options;
        const target = options[Math.floor(rng() * options.length)].target;
        return { type: 'poison', target: target == null ? options[0].target! : target };
    }
    if (ids.indexOf('witch_pass') >= 0) {
        return { type: 'witch_pass' };
    }
    if (ids.indexOf('explode') >= 0 && rng() < 0.08) {
        return { type: 'explode', target: pick(engine, seat, 'explode', rng, 0.2) };
    }
    if (ids.indexOf('guard') >= 0) {
        return { type: 'guard', target: pick(engine, seat, 'guard', rng, 0.25) };
    }
    if (ids.indexOf('knife') >= 0) {
        const mine = view.wolfPicks.find((item) => item.seat === seat);
        const other = view.wolfPicks.find((item) => item.seat !== seat);
        if (other && (!mine || mine.target !== other.target)) {
            return { type: 'knife', target: other.target };
        }
        if (mine) {
            return null;
        }
        const prey = view.seats.filter((item) => item.alive && !item.wolfMate);
        const target = prey.length ? prey[Math.floor(rng() * prey.length)].seat : null;
        return { type: 'knife', target };
    }
    if (ids.indexOf('seer') >= 0) {
        const options = engine.getPicker(seat, 'seer')!.options;
        return { type: 'seer', target: options[Math.floor(rng() * options.length)].target! };
    }
    if (ids.indexOf('vote') >= 0) {
        const options = engine.getPicker(seat, 'vote')!.options;
        const concrete = options.filter((option) => option.target != null);
        const pool = concrete.length && rng() < 0.85 ? concrete : options;
        return { type: 'vote', target: pool[Math.floor(rng() * pool.length)].target };
    }
    const self = view.seats.find((item) => item.seat === seat);
    if (ids.indexOf('campaign') >= 0 && !self?.campaigning && rng() < 0.45) {
        return { type: 'campaign' };
    }
    if (ids.indexOf('step_down') >= 0 && rng() < 0.08) {
        return { type: 'step_down' };
    }
    if (ids.indexOf('accept_exile') >= 0) {
        return { type: 'accept_exile' };
    }
    return null;
}

function pick(engine: GameEngine, seat: number, action: string, rng: () => number, emptyChance: number): number | null {
    const options = engine.getPicker(seat, action)!.options;
    const living = options.filter((option) => option.target != null);
    if (!living.length || rng() < emptyChance) {
        return null;
    }
    return living[Math.floor(rng() * living.length)].target;
}
