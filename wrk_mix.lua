-- wrk_mix.lua
-- 80% 读（GET /api/user/page），20% 写（POST /api/social/message/send）
-- 使用方法：
-- wrk -t12 -c1000 -d60s -s wrk_mix.lua 'http://localhost:8080'

math.randomseed(os.time())

-- 随机生成简单消息内容，防止重复完全相同的请求体
local function random_message()
  return "load-test-" .. tostring(math.random(100000,999999))
end

request = function()
  -- 生成 1-100 随机数，<=80 则为读操作
  local p = math.random(1,100)
  if p <= 80 then
    -- 读操作：分页查询
    return wrk.format(nil, "/api/user/page?pageNum=1&pageSize=10")
  else
    -- 写操作：发送消息（需要 User-Id 请求头）
    local body = string.format('{"toUserId":%d,"content":"%s"}', 2, random_message())
    local headers = {
      ["Content-Type"] = "application/json",
      ["User-Id"] = "1",
      ["X-Load-Test"] = "true"
    }
    return wrk.format("POST", "/api/social/message/send", headers, body)
  end
end
