use aes::Aes128;
use block_modes::{BlockMode, Cbc};
use block_modes::block_padding::Pkcs7;
use hex_literal::hex;
use std::io::{self, Write};

type Aes128Cbc = Cbc<Aes128, Pkcs7>;

fn main() {
    //The key and IV must be 16 bytes.
    let key = b"anexamplekey123";       // 16 symb
    let iv = b"uniqueinitvector";        // 16 symb

    // Init of encoder
    let cipher = Aes128Cbc::new_from_slices(key, iv)
        .expect("Ошибка инициализации шифратора");

    println!("Enter text to encrypt:");
    let mut input = String::new();
    io::stdin().read_line(&mut input).expect("Ошибка чтения ввода");
    let input = input.trim();

    // Data encryption
    let ciphertext = cipher.encrypt_vec(input.as_bytes());

    println!("Encrypted (hex): {}", hex::encode(&ciphertext));
}
