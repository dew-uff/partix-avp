<results> {
let $auction := doc("xmark100m.xml") return
 for $a in
  $auction/site/closed_auctions/closed_auction/annotation/description/parlist/
   listitem/
   parlist/
   listitem/
   text/
   emph/
   keyword/
   text()
 return <text>{$a}</text>
} </results>