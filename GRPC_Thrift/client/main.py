import argparse

from Client import Client

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--bank_port", type=int, required=True)
    args = parser.parse_args()

    Client(args.bank_port).launch()





