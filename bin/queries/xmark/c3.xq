<results> {
let $auction := doc("xmark100m.xml") return
 for $b in $auction/site/open_auctions/open_auction
 where zero-or-one($b/bidder[1]/increase/text()) * 2 <= $b/bidder[last()]/increase/text()
 return
    <increase_instance>
       {<increase first="{$b/bidder[1]/increase/text()}" last="{$b/bidder[last()]/increase/text()}"/>}
    </increase_instance>
} </results>