select p.category_id ,p.product_id,count( o.user_id) as order_count
FROM order_items oi
JOIN orders o ON oi.order_id=o.order_id
join products p on oi.product_id = p.product_id 
group by p.category_id ,p.product_id
order by order_count desc