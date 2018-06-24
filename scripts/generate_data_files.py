import random
import argparse

parser = argparse.ArgumentParser(description='Process coordinates.')
parser.add_argument('--min_lon', metavar='N', type=int,
                    help='max_lan is a required')
parser.add_argument('--max_lon', metavar='N', type=int,
                    help='max_lon is a required')
parser.add_argument('--min_lan', metavar='N', type=int,
                    help='min_lan is a required')
parser.add_argument('--max_lan', metavar='N', type=int,
                    help='max_lan is a required')

args = parser.parse_args()


def generate_grid(min_lon, max_lon, min_lan, max_lan):
    file = open("./src/main/resources/grid.txt", "w")
    for x in range(min_lon, max_lon):
        for y in range(min_lan, max_lan):
            val = str(x) + ',' + str(y) + ',' + str(random.uniform(2.0, 30.0)) + '\n'
            file.write(val)

def generate_user(min_lon, max_lon, min_lan, max_lan):
    file = open("./src/main/resources/user_labels.txt", "w")
    for x in range(1, 1000000):
        val = str(x) + ',' + str(round(random.uniform(min_lon, max_lon), 4)) + ',' + str(round(random.uniform(min_lan, max_lan), 4)) + '\n'
        file.write(val)

if __name__ == '__main__':
    generate_grid(args.min_lon, args.max_lon, args.min_lan, args.max_lan)
    generate_user(args.min_lon, args.max_lon, args.min_lan, args.max_lan)