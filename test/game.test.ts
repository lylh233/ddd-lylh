import assert from 'node:assert/strict';
import { test } from 'node:test';
import { GameEngine } from '../assets/scripts/game/GameEngine.ts';
import { shuffleRoles } from '../assets/scripts/game/presets.ts';
import { GameAction, RoleId } from '../assets/scripts/game/types.ts';

function game(roles: RoleId[]): GameEngine {
    const engine = new GameEngine();
    engine.start(roles.map((_, index) => `P${index + 1}`), roles);
    return engine;
}

function must(engine: GameEngine, seat: number, action: GameAction): void {
    const result = engine.act(seat, action);
    assert.equal(result.ok, true, result.error);
}

function ids(engine: GameEngine, seat: number): string[] {
    return engine.view(seat).actions.map((action) => action.id);
}

function runNight(engine: GameEngine, knife: number | null, options?: {
    guard?: number | null;
    witch?: 'pass' | 'save' | number;
}): void {
    for (let step = 0; step < 20; step += 1) {
        if (engine.phase === 'dawn_announce' || engine.phase.indexOf('day') === 0 || engine.phase.indexOf('resolve') === 0 || engine.phase === 'settlement') {
            return;
        }
        let moved = false;
        for (const player of engine.players) {
            const actions = ids(engine, player.seat);
            if (actions.indexOf('guard') >= 0) {
                must(engine, player.seat, { type: 'guard', target: options?.guard ?? null });
                moved = true;
            } else if (actions.indexOf('knife_confirm') >= 0) {
                must(engine, player.seat, { type: 'knife_confirm' });
                moved = true;
            } else if (actions.indexOf('knife') >= 0) {
                const pick = engine.view(player.seat).wolfPicks.find((item) => item.seat === player.seat);
                if (!pick || pick.target !== knife) {
                    must(engine, player.seat, { type: 'knife', target: knife });
                    moved = true;
                }
            } else if (actions.indexOf('seer') >= 0) {
                const target = engine.players.find((item) => item.alive && item.role !== 'seer')!.seat;
                must(engine, player.seat, { type: 'seer', target });
                moved = true;
            } else if (options?.witch === 'save' && actions.indexOf('antidote') >= 0) {
                must(engine, player.seat, { type: 'antidote' });
                moved = true;
            } else if (typeof options?.witch === 'number' && actions.indexOf('poison') >= 0) {
                must(engine, player.seat, { type: 'poison', target: options.witch });
                moved = true;
            } else if (actions.indexOf('witch_pass') >= 0) {
                must(engine, player.seat, { type: 'witch_pass' });
                moved = true;
            }
        }
        if (!moved) {
            if (engine.phase === 'night_wolf') {
                engine.tick(20000);
            } else if (engine.phase === 'night_seer' || engine.phase === 'night_witch') {
                engine.tick(10000);
            } else {
                break;
            }
        }
    }
}

function toCampaign(engine: GameEngine): void {
    if (engine.phase === 'dawn_announce') {
        engine.tick(5000);
    }
}

test('守卫挡住刀，女巫看不到解药', () => {
    const engine = game(['guard', 'werewolf', 'villager', 'witch']);
    must(engine, 1, { type: 'guard', target: 3 });
    must(engine, 2, { type: 'knife', target: 3 });
    engine.tick(20000);
    must(engine, 2, { type: 'knife_confirm' });
    assert.equal(ids(engine, 4).indexOf('antidote'), -1);
    must(engine, 4, { type: 'witch_pass' });
    assert.equal(engine.prompt.indexOf('平安夜') >= 0, true);
    assert.equal(engine.players[2].alive, true);
});

test('解药救下被刀的人，毒药单独淘汰', () => {
    const saved = game(['werewolf', 'villager', 'witch', 'seer']);
    must(saved, 1, { type: 'knife', target: 2 });
    saved.tick(20000);
    must(saved, 1, { type: 'knife_confirm' });
    must(saved, 4, { type: 'seer', target: 1 });
    assert.equal(saved.view(4).notice!.text, '1号是狼人');
    assert.equal(ids(saved, 3).indexOf('antidote') >= 0, true);
    must(saved, 3, { type: 'antidote' });
    assert.equal(saved.players[1].alive, true);
    assert.equal(saved.view(2).logs.some((entry) => entry.text.indexOf('解药') >= 0), false);

    const poisoned = game(['werewolf', 'villager', 'witch', 'villager']);
    runNight(poisoned, null, { witch: 2 });
    assert.equal(poisoned.prompt.indexOf('2号') >= 0, true);
    assert.equal(poisoned.players[1].alive, false);
    assert.equal(poisoned.players[1].deadReason, 'killed');
});

test('狼人目标不一致不能确定', () => {
    const engine = game(['werewolf', 'werewolf', 'villager', 'seer']);
    must(engine, 1, { type: 'knife', target: 3 });
    must(engine, 2, { type: 'knife', target: 4 });
    engine.tick(20000);
    assert.equal(engine.act(1, { type: 'knife_confirm' }).ok, false);
    assert.deepEqual(engine.view(1).wolfPicks.map((item) => item.target), [3, 4]);
    must(engine, 2, { type: 'knife', target: 3 });
    must(engine, 1, { type: 'knife_confirm' });
    assert.equal(engine.phase, 'night_seer');
});

test('不能连续两晚守护同一人', () => {
    const engine = game(['guard', 'werewolf', 'villager', 'seer']);
    runNight(engine, null, { guard: 3 });
    toCampaign(engine);
    must(engine, 2, { type: 'campaign' });
    must(engine, 1, { type: 'start_speech' });
    must(engine, 1, { type: 'start_speech' });
    must(engine, 1, { type: 'start_vote' });
    engine.tick(30000);
    assert.equal(engine.day, 2);
    assert.equal(engine.act(1, { type: 'guard', target: 3 }).ok, false);
    must(engine, 1, { type: 'guard', target: null });
});

test('多人上警后由非上警玩家投票产生警长', () => {
    const engine = game(['villager', 'villager', 'villager', 'werewolf']);
    runNight(engine, null);
    toCampaign(engine);
    must(engine, 2, { type: 'campaign' });
    must(engine, 3, { type: 'campaign' });
    const campaignLogs = engine.logs.filter((entry) => entry.text.indexOf('上警') >= 0);
    assert.equal(campaignLogs.length, 1);
    assert.equal(campaignLogs[0].text, '2号，3号玩家上警');
    must(engine, 1, { type: 'start_speech' });
    assert.equal(engine.phase, 'day_campaign_speech');
    must(engine, 1, { type: 'start_vote' });
    must(engine, 1, { type: 'vote', target: 2 });
    must(engine, 4, { type: 'vote', target: 2 });
    assert.equal(engine.players[1].isSheriff, true);
    assert.equal(engine.prompt, '2号玩家当选警长');
});

test('警长放逐票按 1.5 计算', () => {
    const engine = game(['villager', 'villager', 'villager', 'werewolf']);
    runNight(engine, null);
    toCampaign(engine);
    must(engine, 1, { type: 'campaign' });
    must(engine, 1, { type: 'start_speech' });
    assert.equal(engine.players[0].isSheriff, true);
    must(engine, 1, { type: 'start_speech' });
    must(engine, 1, { type: 'start_vote' });
    must(engine, 1, { type: 'vote', target: 2 });
    must(engine, 2, { type: 'vote', target: 3 });
    must(engine, 3, { type: 'vote', target: 2 });
    must(engine, 4, { type: 'vote', target: 3 });
    assert.equal(engine.players[1].alive, false);
    assert.equal(engine.players[1].deadReason, 'exiled');
    assert.equal(engine.logs.some((entry) => entry.text.indexOf('1号，3号投给了2号') >= 0), true);
});

test('白痴亮身份后不放逐，警长会先走警徽流', () => {
    const engine = game(['idiot', 'werewolf', 'villager', 'witch']);
    runNight(engine, null);
    toCampaign(engine);
    must(engine, 1, { type: 'campaign' });
    must(engine, 1, { type: 'start_speech' });
    must(engine, 1, { type: 'start_speech' });
    must(engine, 1, { type: 'start_vote' });
    must(engine, 1, { type: 'vote', target: 1 });
    must(engine, 2, { type: 'vote', target: 1 });
    must(engine, 3, { type: 'vote', target: 1 });
    must(engine, 4, { type: 'vote', target: 1 });
    assert.equal(engine.phase, 'day_idiot');
    must(engine, 1, { type: 'reveal_idiot' });
    assert.equal(engine.phase, 'resolve_badge');
    assert.equal(engine.players[0].alive, true);
    must(engine, 1, { type: 'badge', target: null });
    assert.equal(engine.players[0].alive, true);
    assert.equal(engine.players[0].revealedIdiot, true);
    assert.equal(engine.players[0].isSheriff, false);
    assert.equal(engine.day, 2);
});

test('白狼王自爆结束白天', () => {
    const engine = game(['whiteWolf', 'werewolf', 'villager', 'villager']);
    runNight(engine, null);
    toCampaign(engine);
    must(engine, 1, { type: 'explode', target: 3 });
    assert.equal(engine.players[0].alive, false);
    assert.equal(engine.players[2].alive, false);
    assert.equal(engine.day, 2);
    assert.equal(engine.phase, 'night_wolf');
});

test('警长死亡前先警徽流', () => {
    const engine = game(['werewolf', 'villager', 'witch', 'seer']);
    runNight(engine, null);
    toCampaign(engine);
    must(engine, 2, { type: 'campaign' });
    must(engine, 1, { type: 'start_speech' });
    must(engine, 1, { type: 'start_speech' });
    must(engine, 1, { type: 'start_vote' });
    engine.tick(30000);
    assert.equal(engine.players[1].isSheriff, true);
    runNight(engine, 2);
    assert.equal(engine.phase, 'resolve_badge');
    assert.equal(engine.players[1].alive, true);
    must(engine, 2, { type: 'badge', target: 3 });
    assert.equal(engine.players[1].alive, false);
    assert.equal(engine.players[2].isSheriff, true);
});

test('阵营结算与猎人开枪', () => {
    const wolfWin = game(['werewolf', 'villager', 'seer', 'witch']);
    runNight(wolfWin, 2);
    assert.equal(wolfWin.winner, 'wolf');
    assert.equal(wolfWin.phase, 'settlement');

    const goodWin = game(['werewolf', 'hunter', 'villager', 'seer']);
    runNight(goodWin, 2);
    assert.equal(goodWin.phase, 'resolve_hunter');
    assert.equal(goodWin.players[1].alive, false);
    must(goodWin, 2, { type: 'shoot', target: 1 });
    assert.equal(goodWin.winner, 'good');
    assert.equal(goodWin.view(2).logs.some((entry) => entry.text === '你射杀了1号玩家'), true);
    assert.equal(goodWin.view(3).logs.some((entry) => entry.text.indexOf('射杀') >= 0), false);
});

test('女巫毒死猎人不触发开枪', () => {
    const poisoned = game(['werewolf', 'hunter', 'witch', 'villager']);
    runNight(poisoned, null, { witch: 2 });
    assert.equal(poisoned.phase, 'dawn_announce');
    assert.equal(poisoned.players[1].alive, false);
    assert.equal(ids(poisoned, 2).indexOf('shoot'), -1);

    const both = game(['werewolf', 'hunter', 'witch', 'villager']);
    runNight(both, 2, { witch: 2 });
    assert.equal(both.phase, 'dawn_announce');
    assert.equal(ids(both, 2).indexOf('shoot'), -1);
});

test('白天放逐猎人开枪，带走的人计入下一夜', () => {
    const engine = game(['hunter', 'werewolf', 'villager', 'villager', 'seer', 'villager']);
    runNight(engine, null);
    toCampaign(engine);
    must(engine, 2, { type: 'campaign' });
    must(engine, 1, { type: 'start_speech' });
    must(engine, 1, { type: 'start_speech' });
    must(engine, 1, { type: 'start_vote' });
    [1, 2, 3, 4, 5, 6].forEach((seat) => must(engine, seat, { type: 'vote', target: seat === 1 ? null : 1 }));
    assert.equal(engine.phase, 'resolve_hunter');
    must(engine, 1, { type: 'shoot', target: 3 });
    assert.equal(engine.players[2].alive, false);
    assert.equal(engine.players[2].deadReason, 'killed');
    assert.equal(engine.view(1).logs.some((entry) => entry.text === '你射杀了3号玩家'), true);
    assert.equal(engine.view(2).logs.some((entry) => entry.text.indexOf('射杀') >= 0), false);
    assert.equal(engine.view(4).logs.some((entry) => entry.text.indexOf('射杀') >= 0), false);
    assert.equal(engine.logs.some((entry) => entry.day === 1 && entry.text.indexOf('3号玩家被淘汰') >= 0), false);
    assert.equal(engine.day, 2);
    runNight(engine, 4);
    assert.equal(engine.prompt.indexOf('3号') >= 0, true);
    assert.equal(engine.prompt.indexOf('4号') >= 0, true);
    assert.equal(engine.logs.some((entry) => entry.day === 2 && entry.text.indexOf('昨晚淘汰的玩家是3号、4号') >= 0), true);
    assert.equal(engine.logs.some((entry) => entry.day === 2 && entry.text === '3号玩家被淘汰'), true);
    assert.equal(engine.logs.some((entry) => entry.day === 2 && entry.text === '4号玩家被淘汰'), true);
});

test('死亡预言家仍停留 10 秒，1 号淘汰后仍可开始发言', () => {
    const engine = game(['villager', 'werewolf', 'villager', 'witch']);
    runNight(engine, 1);
    assert.equal(engine.players[0].alive, false);
    toCampaign(engine);
    assert.equal(ids(engine, 1).indexOf('start_speech') >= 0, true);
    assert.equal(ids(engine, 1).indexOf('campaign'), -1);
    assert.equal(ids(engine, 3).indexOf('start_speech'), -1);

    const seer = game(['werewolf', 'seer', 'witch', 'villager']);
    runNight(seer, 2);
    toCampaign(seer);
    must(seer, 4, { type: 'campaign' });
    must(seer, 1, { type: 'start_speech' });
    must(seer, 1, { type: 'start_speech' });
    must(seer, 1, { type: 'start_vote' });
    seer.tick(30000);
    must(seer, 1, { type: 'knife', target: null });
    seer.tick(20000);
    must(seer, 1, { type: 'knife_confirm' });
    assert.equal(seer.phase, 'night_seer');
    seer.tick(9999);
    assert.equal(seer.phase, 'night_seer');
    seer.tick(1);
    assert.equal(seer.phase, 'night_witch');
});

test('身份随机发放且人数不变', () => {
    const roles: RoleId[] = ['werewolf', 'seer', 'witch', 'villager'];
    const dealt = shuffleRoles(roles, () => 0);
    assert.notDeepEqual(dealt, roles);
    assert.deepEqual(dealt.slice().sort(), roles.slice().sort());
});

test('开始发言和开始投票不会超时跳过', () => {
    const engine = game(['villager', 'villager', 'villager', 'werewolf']);
    runNight(engine, null);
    toCampaign(engine);
    engine.tick(30000);
    assert.equal(engine.phase, 'day_campaign');
    must(engine, 2, { type: 'campaign' });
    must(engine, 3, { type: 'campaign' });
    must(engine, 1, { type: 'start_speech' });
    assert.equal(engine.phase, 'day_campaign_speech');
    engine.tick(30000);
    assert.equal(engine.phase, 'day_campaign_speech');
    must(engine, 1, { type: 'start_vote' });
    assert.equal(engine.phase, 'day_sheriff_vote');
});

test('投票过程可以看到谁投了谁', () => {
    const engine = game(['villager', 'villager', 'villager', 'werewolf']);
    runNight(engine, null);
    toCampaign(engine);
    must(engine, 2, { type: 'campaign' });
    must(engine, 3, { type: 'campaign' });
    must(engine, 1, { type: 'start_speech' });
    must(engine, 1, { type: 'start_vote' });
    must(engine, 1, { type: 'vote', target: 2 });
    const midway = engine.view(4);
    assert.equal(midway.voteText.indexOf('1号投给了2号') >= 0, true);
    assert.deepEqual(midway.ballots, [{ seat: 1, target: 2 }]);
    must(engine, 4, { type: 'vote', target: null });
    assert.equal(engine.view(1).voteText.indexOf('4号玩家弃权') >= 0, true);
    assert.equal(engine.view(1).ballots.length, 2);
});

test('放逐投票可以弃权，并写入日志', () => {
    const engine = game(['villager', 'villager', 'villager', 'werewolf']);
    runNight(engine, null);
    toCampaign(engine);
    must(engine, 1, { type: 'campaign' });
    must(engine, 1, { type: 'start_speech' });
    must(engine, 1, { type: 'start_speech' });
    must(engine, 1, { type: 'start_vote' });
    assert.equal(engine.getPicker(2, 'vote')!.options.some((option) => option.target == null && option.label === '弃权'), true);
    must(engine, 1, { type: 'vote', target: null });
    must(engine, 2, { type: 'vote', target: null });
    must(engine, 3, { type: 'vote', target: null });
    must(engine, 4, { type: 'vote', target: null });
    assert.equal(engine.logs.some((entry) => entry.text.indexOf('1号，2号，3号，4号玩家弃权') >= 0), true);
});

test('只有最高票并列才重新投票', () => {
    const engine = game(['villager', 'villager', 'villager', 'villager', 'villager', 'villager', 'werewolf', 'seer']);
    runNight(engine, null);
    toCampaign(engine);
    must(engine, 2, { type: 'campaign' });
    must(engine, 3, { type: 'campaign' });
    must(engine, 4, { type: 'campaign' });
    must(engine, 1, { type: 'start_speech' });
    must(engine, 1, { type: 'start_vote' });
    must(engine, 1, { type: 'vote', target: 2 });
    must(engine, 5, { type: 'vote', target: 2 });
    must(engine, 6, { type: 'vote', target: 2 });
    must(engine, 7, { type: 'vote', target: 3 });
    must(engine, 8, { type: 'vote', target: 4 });
    assert.equal(engine.players[1].isSheriff, true);
    assert.notEqual(engine.phase, 'day_sheriff_vote');

    const tied = game(['villager', 'villager', 'villager', 'villager', 'villager', 'villager', 'werewolf', 'seer']);
    runNight(tied, null);
    toCampaign(tied);
    must(tied, 2, { type: 'campaign' });
    must(tied, 3, { type: 'campaign' });
    must(tied, 4, { type: 'campaign' });
    must(tied, 1, { type: 'start_speech' });
    must(tied, 1, { type: 'start_vote' });
    must(tied, 1, { type: 'vote', target: 2 });
    must(tied, 5, { type: 'vote', target: 2 });
    must(tied, 6, { type: 'vote', target: 3 });
    must(tied, 7, { type: 'vote', target: 3 });
    must(tied, 8, { type: 'vote', target: 4 });
    assert.equal(tied.phase, 'day_sheriff_vote');
    assert.equal(tied.prompt.indexOf('再次投票') >= 0, true);
});

test('刀人按存活狼人过半，空刀至少一半', () => {
    const split = game(['werewolf', 'werewolf', 'villager', 'villager']);
    must(split, 1, { type: 'knife', target: 3 });
    split.tick(20000);
    assert.equal(split.act(1, { type: 'knife_confirm' }).ok, false);
    must(split, 2, { type: 'knife', target: 3 });
    must(split, 1, { type: 'knife_confirm' });
    assert.equal(split.logs.some((entry) => entry.text === '刀了3号玩家'), true);

    const empty = game(['werewolf', 'werewolf', 'villager', 'villager']);
    must(empty, 1, { type: 'knife', target: null });
    assert.equal(empty.act(1, { type: 'knife_confirm' }).ok, false);
    empty.tick(20000);
    must(empty, 1, { type: 'knife_confirm' });
    assert.equal(empty.logs.some((entry) => entry.text === '狼人空刀'), true);

    const missed = game(['werewolf', 'werewolf', 'villager', 'villager']);
    must(missed, 1, { type: 'knife', target: 3 });
    missed.tick(30000);
    assert.equal(missed.logs.some((entry) => entry.text === '狼人未能达成刀口'), true);
    assert.equal(missed.players[2].alive, true);
});

test('开局身份只写给本人', () => {
    const engine = game(['werewolf', 'villager', 'villager', 'seer']);
    assert.equal(engine.view(1).logs.some((entry) => entry.text === '你本局的身份是狼人'), true);
    assert.equal(engine.view(2).logs.some((entry) => entry.text === '你本局的身份是狼人'), false);
    assert.equal(engine.view(2).logs.filter((entry) => entry.text === '你本局的身份是居民').length, 1);
    assert.equal(engine.view(3).logs.filter((entry) => entry.text === '你本局的身份是居民').length, 1);
    assert.equal(engine.view(4).logs.some((entry) => entry.text === '你本局的身份是预言家'), true);
    assert.equal(engine.view(1).logs.some((entry) => entry.text === '你本局的身份是预言家'), false);
});

test('第一日狼人身份只写给狼人阵营', () => {
    const engine = game(['werewolf', 'whiteWolf', 'villager', 'seer']);
    const wolfLog = engine.view(1).logs.find((entry) => entry.text.indexOf('白狼王') >= 0);
    assert.equal(wolfLog != null && wolfLog.text.indexOf('1号是狼人') >= 0, true);
    assert.equal(engine.view(2).logs.some((entry) => entry.text.indexOf('1号是狼人') >= 0), true);
    assert.equal(engine.view(3).logs.some((entry) => entry.text.indexOf('白狼王') >= 0), false);
    assert.equal(engine.view(4).logs.some((entry) => entry.text.indexOf('狼人') >= 0 && entry.audience === 'wolf'), false);
});

test('预言家日志仅自己可见', () => {
    const engine = game(['werewolf', 'villager', 'witch', 'seer']);
    runNight(engine, null);
    assert.equal(engine.view(4).logs.some((entry) => entry.text.indexOf('查验') >= 0), true);
    assert.equal(engine.view(2).logs.some((entry) => entry.text.indexOf('查验') >= 0), false);
    assert.equal(engine.view(1).logs.some((entry) => entry.text.indexOf('空刀') >= 0), true);
    assert.equal(engine.view(2).logs.some((entry) => entry.text.indexOf('空刀') >= 0), false);
});
