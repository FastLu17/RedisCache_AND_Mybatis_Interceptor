--[[
不足之处：
　　1. Redis 配合 Lua 脚本基于单个写节点的 Redis集群，无法适用于多个写节点的Redis集群；
　　2. Redis 执行 Lua 脚本 具有了原子性， 但是 Lua脚本内的 多个写操作 没有实现 原子性(事务)。
--]]

-- redis keys
local user_today_got_key = KEYS[1]; --Lua下表从1开始
local user_got_key = KEYS[2];
local total_got_key = KEYS[3];
--redis args
local user_per_day_max = tonumber(ARGV[1]);
local user_max = tonumber(ARGV[2]);
local max = tonumber(ARGV[3]);
local userId = ARGV[4];
local couponId = ARGV[5];

-- 用户每天可领券的最大数量
local user_today_got = redis.call("hget", user_today_got_key, userId);
if(user_today_got and tonumber(user_today_got) >= user_per_day_max) then
    return 1; --fail
end

-- 用户可领券的最大数量
local user_got = redis.call("hget",user_got_key,couponId);
if(user_got and tonumber(user_got) >= user_max) then
    return 2; --fail
end

-- 券的最大数量
local total_got = redis.call("hget",total_got_key,couponId);
if(total_got and tonumber(total_got) >= max) then
    return 3; --fail
end

redis.call("hincrby",user_today_got_key, userId,1);
redis.call("hincrby",user_got_key, couponId,1);
redis.call("hincrby",total_got_key, couponId,1);
return 0; -- success