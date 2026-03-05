local key = KEYS[1]
local now = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local limit = tonumber(ARGV[3])

local clearBefore = now - window * 1000

redis.call('ZREMRANGEBYSCORE', key, '-inf', clearBefore)

local count = redis.call('ZCARD', key)

if count < limit then
    redis.call('ZADD', key, now, now .. '-' .. math.random(1, 1000000))
    redis.call('PEXPIRE', key, window * 1000)
    return 1
else
    return 0
end