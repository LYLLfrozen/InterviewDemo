#!/bin/bash
# 迁移Redis中的文章到MySQL

echo "开始迁移Redis文章到MySQL..."

# 获取所有文章key
keys=$(redis-cli --scan --pattern "article:*" | grep -v "article:order" | grep -v "article:list")

count=0
for key in $keys; do
    # 获取文章JSON数据
    article_json=$(redis-cli GET "$key")
    
    if [ ! -z "$article_json" ] && [ "$article_json" != "(nil)" ]; then
        echo "迁移: $key"
        
        # 通过API创建文章（会自动保存到MySQL）
        curl -s -X POST http://localhost:8080/api/mainPage \
          -H "Content-Type: application/json" \
          -d "$article_json" > /dev/null
        
        ((count++))
        echo "  -> 已迁移 $count 篇文章"
    fi
done

echo "迁移完成！共迁移 $count 篇文章"
echo "验证MySQL中的文章数量："
mysql -u root -p -e "USE \`0813-demo\`; SELECT COUNT(*) as total FROM article;"
