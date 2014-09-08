<results>
{
for $it in
doc('xmlDataBaseXmark.xml')/site/regions/samerica/item
for $pe in
doc('xmlDataBaseXmark.xml')/site/people/person
where $pe/profile/interest/@category =
$it/incategory/@category
and $it/incategory/@category = "category251"
and $pe/profile/education = "Graduate School"
return
<people>
{$pe}
</people>
} </results>