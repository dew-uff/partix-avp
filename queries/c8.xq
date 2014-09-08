<results>
{
for $pe in
doc('xmlDataBaseXmark.xml')/site/people/person
let $int := $pe/profile/interest
where $pe/profile/business = "Yes"
and count($int) > 1
order by $pe/name
return
<people>
{$pe}
</people>
} </results>