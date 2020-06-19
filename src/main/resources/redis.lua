-- redis.call('set',KEYS[1],ARGV[1])

-- return 'get value：'..redis.call('get',KEYS[1])

local stock = redis.call('get',KEYS[1])
if tonumber(stock) <= 0 then
    return 0
else
    redis.call('decr',KEYS[1])
    local count = redis.call('incr',KEYS[2])
    return count
end
