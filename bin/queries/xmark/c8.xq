<results> {
let $auction := doc("xmark100m.xml") return
 for $p in $auction/site/people/person
 let $a :=
  for $t in $auction/site/closed_auctions/closed_auction
  where $t/buyer/@person = $p/@id
  return $t
 return <item person="{$p/name/text()}">{count($a)}</item>
} </results>
