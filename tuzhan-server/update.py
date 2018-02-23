import csv, json, io

cfile = file('data.csv', 'rb')
reader = csv.reader(cfile)

data = {}

for row in reader:
    card = {}
    card['answersRaw'] = row[2] + ';' + row[3]
    card['credit'] = 'placeholder'
    card['imageURL'] = row[1]

    data.setdefault(row[0], [])
    data[row[0]].append(card)

with open('cards.json', 'w') as outfile:
  json.dump(data, outfile, ensure_ascii=False)

#serverside

themes = []
counts = []

for theme, cards in data.iteritems():
    themes.append(theme)
    counts.append(len(cards))

with open('data.json', 'w') as outfile:
  json.dump({
    'themes'    : themes,
    'cardCounts': counts
  }, outfile, ensure_ascii=False)
