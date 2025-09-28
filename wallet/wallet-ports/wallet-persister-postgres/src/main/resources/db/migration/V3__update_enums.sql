UPDATE currency_off_chain_gateway
SET transfer_method = CASE
                          WHEN transfer_method = 'Card2card' THEN 'CARD'
                          WHEN transfer_method = 'Sheba' THEN 'SHEBA' END
WHERE transfer_method IN ('Card2card', 'Sheba');


UPDATE withdraws
SET dest_network = CASE WHEN dest_network = 'Card2card' THEN 'CARD' WHEN dest_network = 'Sheba' THEN 'SHEBA' END
WHERE dest_network IN ('Card2card', 'Sheba');



UPDATE terminal
SET type = CASE WHEN type = 'Card2card' THEN 'CARD' WHEN type = 'Sheba' THEN 'SHEBA' END
WHERE type IN ('Card2card', 'Sheba');